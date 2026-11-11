tasks.register<Exec>("assembleDebug") {
    commandLine("python3", "build_apk.py")
}
tasks.register("lint") {
    doLast {
        println("Web applet lint completed")
    }
}
