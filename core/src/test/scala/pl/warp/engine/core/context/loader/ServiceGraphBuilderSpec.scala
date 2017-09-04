package pl.warp.engine.core.context.loader

import java.lang.invoke.MethodHandle

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{Matchers, WordSpecLike}
import pl.warp.engine.core.context.graph.DirectedAcyclicGraph
import pl.warp.engine.core.context.loader.ServiceGraphBuilderSpec._
import pl.warp.engine.core.context.loader.service.{DependencyInfo, ServiceGraphBuilder, ServiceInfo}

import scala.reflect.ClassTag

/**
  * @author Jaca777
  *         Created 2017-09-04 at 10
  */
class ServiceGraphBuilderSpec extends WordSpecLike with Matchers with MockFactory with GraphMatchers with ServiceBuilders {

  "ServiceGraphBuilder" should {

    "build 1-elem service graph" in {
      //given
      val graphBuilder = new ServiceGraphBuilder
      val services = Set(service[A]())

      //when
      val graph = graphBuilder.build(services)

      //then
      val rootNodes = graph.rootNodes
      rootNodes.size should be(1)
      rootNodes.map(_.value).head should be(service[A]())
    }

    "build graph with dependent nodes" in {
      //given
      val graphBuilder = new ServiceGraphBuilder
      val services = Set(
        service[A](),
        service[B](dependencies = List(dep[A]())),
        service[C](dependencies = List(dep[A](), dep[B]()))
      )

      //when
      val graph = graphBuilder.build(services)

      //then
      val rootNodes = graph.rootNodes
      rootNodes.size should be(1)
      rootNodes.map(_.value).head should be(service[A]())
    }

    "use qualifiers to resolve services" in {
      //given
      val graphBuilder = new ServiceGraphBuilder
      val services = Set(
        service[B](dependencies = List(dep[A](qualifier = Some("test")))),
        service[D](qualifier = Some("test")),
        service[E](qualifier = None)
      )

      //when
      val graph = graphBuilder.build(services)

      //then
      graph should containDependency[B -> D]
    }
  }

}

object ServiceGraphBuilderSpec {

  class A

  class B

  class C

  class D extends A

  class E extends A


  trait GraphMatchers {

    class FileEndsWithExtensionMatcher(from: Class[_], to: Class[_]) extends Matcher[DirectedAcyclicGraph[ServiceInfo]] {

      def apply(graph: DirectedAcyclicGraph[ServiceInfo]) = {
        val node = graph.resolveNode(_.t == to)
        val matches = node match {
          case Some(b) => b.leaves.exists(_.value.t == from)
          case None => false
        }
        MatchResult(
          matches,
          s"""Graph did not contain dependency from ${from.getName} to ${to.getName}""",
          s"""Graph contains dependency from ${from.getName} to ${to.getName}"""
        )
      }
    }


    trait DependencyLike[T] {
      def from: Class[_]

      def to: Class[_]
    }

    case class DependencyLikeApply[T](from: Class[_], to: Class[_]) extends DependencyLike[T]

    type ->[A, B]

    implicit def arrowDependencyLike[A: ClassTag, B: ClassTag, Arrow <: ->[A, B]]: DependencyLike[A -> B] = {
      DependencyLikeApply[A -> B](
        implicitly[ClassTag[A]].runtimeClass,
        implicitly[ClassTag[B]].runtimeClass
      )
    }

    def containDependency[C: DependencyLike] = {
      val dependency = implicitly[DependencyLike[C]]
      new FileEndsWithExtensionMatcher(
        dependency.from,
        dependency.to
      )
    }
  }

  trait ServiceBuilders {
    def service[T: ClassTag](
      qualifier: Option[String] = None,
      builder: MethodHandle = null,
      dependencies: List[DependencyInfo] = List.empty
    ): ServiceInfo = {
      ServiceInfo(implicitly[ClassTag[T]].runtimeClass, qualifier, builder, dependencies)
    }

    def dep[T: ClassTag](qualifier: Option[String] = None): DependencyInfo = {
      DependencyInfo(implicitly[ClassTag[T]].runtimeClass, qualifier)
    }
  }

}