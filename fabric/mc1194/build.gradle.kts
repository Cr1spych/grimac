dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings("net.fabricmc:yarn:1.19.4+build.2:v2")
    compileOnly(project(":fabric:mc1161", configuration = "namedElements"))
    compileOnly(project(":fabric:mc1171", configuration = "namedElements"))

    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.87.2+1.19.4"))
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")
}

tasks.compileJava {
    options.release.set(17)
}
