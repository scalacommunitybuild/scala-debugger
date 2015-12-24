package org.scaladebugger.api.profiles.swappable.classes

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.classes.ClassPrepareProfile

import scala.util.Try

/**
 * Represents a swappable profile for class prepare events that redirects the
 * invocation to another profile.
 */
trait SwappableClassPrepareProfile extends ClassPrepareProfile {
  this: SwappableDebugProfile =>

  override def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] = {
    withCurrentProfile.onClassPrepareWithData(extraArguments: _*)
  }
}