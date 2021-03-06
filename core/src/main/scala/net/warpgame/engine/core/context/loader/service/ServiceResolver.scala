package net.warpgame.engine.core.context.loader.service

import java.lang.invoke.{MethodHandle, MethodHandles}
import java.lang.reflect.{AnnotatedElement, Constructor, Parameter}

import net.warpgame.engine.core.context.service.ServiceBuilder
import net.warpgame.engine.core.context.service.{Qualified, ServiceBuilder}
import net.warpgame.engine.core.context.loader.service.ServiceResolver._

/**
  * @author Jaca777
  *         Created 2017-08-27 at 22
  */
private[loader] class ServiceResolver(classResolver: ClassResolver) {

  def resolveServiceInfo(): Set[ServiceInfo] = {
    val classes = classResolver.resolveServiceClasses().par
    val declaredServices = classes.map(toDeclaredServiceInfo).seq
    val arrayCollectiveServices = getCollectiveServices(declaredServices)
    declaredServices ++ arrayCollectiveServices
  }

  def toDeclaredServiceInfo(serviceClass: Class[_]): ServiceInfo = {
    val builderConstructor = findBuilderConstructor(serviceClass)
    val builderHandle = toMethodHandle(builderConstructor)
    val dependencies = getDependencies(builderConstructor)
    val qualifier = getQualifier(serviceClass)
    ServiceInfo(serviceClass, qualifier, builderHandle, dependencies.toList)
  }

  private def findBuilderConstructor(serviceClass: Class[_]): Constructor[_] =
    serviceClass.getConstructors match {
      case Array(constr) => constr
      case constrs: Array[Constructor[_]] =>
        getExplicitBuilderConstructor(constrs, serviceClass.getName)
      case Array() =>
        throw NoServiceConstructorFoundException(serviceClass.getName)
    }

  private def getExplicitBuilderConstructor(constrs: Array[Constructor[_]], className: String): Constructor[_] = {
    val explicitConstrs = constrs.filter(isExplicitBuilder)
    explicitConstrs match {
      case Array(constr) => constr
      case a if a.length > 1 =>
        throw AmbiguousServiceBuilderDefinition(className)
      case Array() =>
        throw UnableToResolveServiceBuilderException(className)
    }
  }

  private def isExplicitBuilder(constructor: Constructor[_]): Boolean =
    constructor.getAnnotation(classOf[ServiceBuilder]) != null

  private def toMethodHandle(constructor: Constructor[_]): MethodHandle = {
    val lookup = MethodHandles.lookup()
    lookup.unreflectConstructor(constructor)
  }

  private def getDependencies(constr: Constructor[_]): Array[DependencyInfo] = {
    val params = constr.getParameters
    params.map(toDependency)
  }

  private def toDependency(param: Parameter): DependencyInfo =
    DependencyInfo(param.getType, getQualifier(param))

  private def getQualifier(param: AnnotatedElement): Option[String] = {
    val annotation = param.getAnnotation(classOf[Qualified])
    if (annotation != null) {
      Some(annotation.qualifier())
    } else {
      None
    }
  }


  private def getCollectiveServices(
    declaredServices: Set[ServiceInfo]
  ): Set[ServiceInfo] = {
    val arrayDependencies = declaredServices
      .flatMap(s => s.dependencies)
      .filter(d => d.`type`.isArray)
    arrayDependencies.map(toCollectiveServiceInfo(declaredServices))
  }

  private def toCollectiveServiceInfo
    (declaredServices: Set[ServiceInfo])
    (dependencyInfo: DependencyInfo)
  : ServiceInfo = {
    val qualified = findQualified(dependencyInfo.`type`, dependencyInfo.qualifier, declaredServices)
    ServiceInfo(
      dependencyInfo.`type`,
      dependencyInfo.qualifier,
      collectiveServiceBuilder(dependencyInfo, qualified.size),
      qualified
    )
  }

  private def findQualified(`type`: Class[_], qualifier: Option[String], services: Set[ServiceInfo]): List[DependencyInfo] = {
    val componentType = `type`.getComponentType
    services.filter(s => componentType.isAssignableFrom(s.`type`) && qualifier.forall(q => s.qualifier.contains(q)))
      .map(toDependencyInfo)
      .toList
  }

  private def toDependencyInfo(serviceInfo: ServiceInfo): DependencyInfo = {
    DependencyInfo(serviceInfo.`type`, serviceInfo.qualifier)
  }

  private def collectiveServiceBuilder(depenencyInfo: DependencyInfo, size: Int): MethodHandle =
    MethodHandles.identity(depenencyInfo.`type`)
      .asCollector(depenencyInfo.`type`, size)

}

object ServiceResolver {

  case class NoServiceConstructorFoundException(className: String)
    extends RuntimeException(
      s"No public constructors found for service at $className"
    )

  case class AmbiguousServiceBuilderDefinition(className: String)
    extends RuntimeException(
      s"Multiple constructors annotated with @ServiceBuilder found for service at $className"
    )

  case class UnableToResolveServiceBuilderException(className: String)
    extends RuntimeException(
      s"Multiple constructors for service at $className found, but none is annotated with @ServiceBuilder"
    )

}