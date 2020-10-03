
job("Build and test") {
    container("gradle:5.2-jdk8") {
        kotlinScript { api ->
            api.gradle("build test")
        }
    }
}