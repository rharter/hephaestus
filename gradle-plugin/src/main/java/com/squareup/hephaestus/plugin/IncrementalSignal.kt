@file:Suppress("UnstableApiUsage")

package com.squareup.hephaestus.plugin

import com.android.build.gradle.internal.workeractions.WorkerActionServiceRegistry
import com.android.ide.common.process.ProcessException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters.None
import org.gradle.api.services.BuildServiceRegistration
import java.io.IOException

fun registerIncrementalSignalBuildService(project: Project) {
  project.gradle.sharedServices.registerIfAbsent(
      "incrementalSignal",
      IncrementalSignal::class.java
  ) {}
}

@Suppress("UNCHECKED_CAST")
fun getIncrementalSignalBuildService(project: Project): Provider<out IncrementalSignal> =
    (project.gradle.sharedServices.registrations.getByName("incrementalSignal") as BuildServiceRegistration<IncrementalSignal, None>).service


/**
 * Service registry used to store IncrementalSignal services so they are accessible from the worker
 * actions.
 */
var incrementalSignalServiceRegistry: WorkerActionServiceRegistry = WorkerActionServiceRegistry()

/** Intended for use from worker actions. */
@Throws(ProcessException::class, IOException::class)
fun useIncrementalSignal(
    serviceRegistry: WorkerActionServiceRegistry = incrementalSignalServiceRegistry,
    block: (MutableMap<String, Boolean?>) -> Unit) {
  return serviceRegistry.getService(IncrementalSignalServiceKey).service.incremental.let(block)
}

fun getIncrementalSignal(
    projectPath: String,
    serviceRegistry: WorkerActionServiceRegistry = incrementalSignalServiceRegistry): Boolean? {
  return serviceRegistry.getService(IncrementalSignalServiceKey).service.incremental[projectPath]
}

object IncrementalSignalServiceKey: WorkerActionServiceRegistry.ServiceKey<IncrementalSignal> {
  override val type: Class<IncrementalSignal> get() = IncrementalSignal::class.java
}
/** This signal is used to share state between the task above and Kotlin compile tasks. */
abstract class IncrementalSignal : BuildService<None> {
  val incremental = mutableMapOf<String, Boolean?>()
}
