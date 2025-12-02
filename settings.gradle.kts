rootProject.name = "Bootstrap"

// Include the plugin as a composite build so it can be used in the example
includeBuild("bootstrap-gradle")

include("bootstrap-core")
include("example")