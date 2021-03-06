package net.warpgame.engine.core.graph

import scala.annotation.tailrec
import scala.collection.Map


/**
  * @author Jaca777
  *         Created 2015-12-23 at 11
  *         Directed Acyclic Graph implementation for the service loader.
  *         Root node is a node that has no nodes connected to it.
  */
case class DAG[+A](rootNodes: List[Node[A]]) {

  def this(rootNodes: Node[A]*) = this(rootNodes.toList)

  def addNode[B >: A](e: B): DAG[B] = {
    val roots: List[Node[_ >: A <: B]] = rootNodes :+ Node[B](e)
    new DAG[B](roots)
  }

  def addEdge[B >: A](from: B, to: B): DAG[B] = {
    val fromOpt = findNode(from)
    val toOpt = findNode(to)
    (fromOpt, toOpt) match {
      case (None, None) => addEdgeAndCreateNodes(from, to)
      case (Some(fromNode), None) => addEdgeFromExistingNode(fromNode, to)
      case (None, Some(toNode)) => addEdgeToExistingNode(from, toNode)
      case (Some(fromNode), Some(toNode)) => addEdgeBetweenExistingNodes(fromNode, toNode)
    }
  }

  private def addEdgeAndCreateNodes[B >: A](from: B, to: B): DAG[B] = {
    val toNode = Node[B](to)
    val fromNode = Node[B](from, toNode)
      .checkedForCycle()
    val roots = rootNodes :+ fromNode
    new DAG[B](roots)
  }

  private def addEdgeFromExistingNode[B >: A](from: Node[B], to: B): DAG[B] = {
    val newNode = Node[B](to)
    val newLeafs = from.leaves :+ newNode
    val updatedNode = Node(from.value, newLeafs)
    replaceNode(from, updatedNode)
  }

  private def addEdgeToExistingNode[B >: A](from: B, to: Node[B]): DAG[B] = {
    val newNode = Node[B](from, to)
    val roots = (if (rootNodes.contains(to)) {
      rootNodes.filterNot(_ == to)
    } else rootNodes) :+ newNode
    new DAG[B](roots)
  }

  private def addEdgeBetweenExistingNodes[B >: A](from: Node[B], to: Node[B]): DAG[B] = {
    val leafs = from.leaves :+ to
    val updatedNode = Node[B](from.value, leafs)
      .checkedForCycle()
    val updatedGraph = replaceNode(from, updatedNode)
    val roots = updatedGraph.rootNodes.filterNot(_ == to)
    new DAG[B](roots)
  }


  /**
    * Finds a node with a given value
    */
  def findNode[B >: A](value: B): Option[Node[B]] =
    resolveNode(_ == value)


  /**
    * Resolves a node for which the condition evaluates to true
    */
  def resolveNode[B >: A](condition: B => Boolean): Option[Node[B]] = {
    @tailrec
    def resolveAcc(toVisit: List[Node[A]], visitedNodes: Set[A]): Option[Node[B]] = toVisit match {
      case node :: tail if visitedNodes.contains(node.value) =>
        resolveAcc(tail, visitedNodes)
      case node :: _ if condition(node.value) => Some(node)
      case Nil => None
      case node :: tail => resolveAcc(tail ::: node.leaves, visitedNodes + node.value)
    }

    resolveAcc(rootNodes, Set.empty)
  }


  def resolveChildren[B >: A](parentCondition: B => Boolean): List[Node[B]] = {
    resolveNode(parentCondition)
      .get
      .leaves
      .flatMap(n => resolveChildren(_ == n))
  }

  /**
    * Creates new graph with given node replaced
    */
  def replaceNode[B >: A](node: Node[B], newNode: Node[B]): DAG[B] = {
    def update(currNode: Node[A]): Option[Node[B]] = currNode match {
      case `node` => Some(newNode)
      case _ =>
        val updatedLeavesByValue = getUpdatedLeavesByValue(currNode)
        if (updatedLeavesByValue.isEmpty) {
          None
        } else {
          val newLeaves = currNode.leaves
            .map(leaf => updatedLeavesByValue.getOrElse(leaf.value, leaf))
          Some(Node(currNode.value, newLeaves))
        }
    }

    def getUpdatedLeavesByValue(parentNode: Node[A]): Map[B, Node[B]] = {
      parentNode.leaves
        .map(update)
        .collect {
          case Some(leaf) => leaf
        }.map(leaf => leaf.value -> leaf)
        .toMap
    }

    val updatedRoots = rootNodes
      .map(root => (root, update(root)))
      .map {
        case (_, Some(updatedRoot)) => updatedRoot
        case (oldRoot, None) => oldRoot
      }

    DAG[B](updatedRoots)
  }

  @tailrec
  final def accept[B >: A, C <: GraphVisitor[B, C]](
    visitor: C,
    at: List[Node[B]] = rootNodes
  ): C = at match {
    case Nil =>
      visitor
    case node :: tail =>
      accept(node.accept(visitor), tail)
  }

}

object DAG {
  def apply[V](rootNodes: Node[V]*): DAG[V] = DAG(rootNodes.toList)

  def empty[A] = new DAG[A](List.empty[Node[A]])
}
