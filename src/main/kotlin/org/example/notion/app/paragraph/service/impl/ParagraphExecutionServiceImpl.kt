package org.example.notion.app.paragraph.service.impl

import org.example.notion.app.paragraph.service.DockerConfig
import org.example.notion.app.paragraph.service.ParagraphExecutionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class ParagraphExecutionServiceImpl(val dockerConfig: DockerConfig) : ParagraphExecutionService {


    companion object {
        private val logger = LoggerFactory.getLogger(ParagraphExecutionServiceImpl::class.java)
        private const val CODE_EXECUTION_ERROR = "Error while execution code: %s"
    }

    @Async
    override fun executeParagraph(code: String): CompletableFuture<String> {

        return CompletableFuture.supplyAsync {
            try {
                val processBuilder = ProcessBuilder("docker", "exec", dockerConfig.image, "python3", "-c", code)
                processBuilder.redirectErrorStream(true)

                val process = processBuilder.start()

                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output
            } catch (e: Exception) {
                e.printStackTrace()
                logger.error(CODE_EXECUTION_ERROR.format(e.message))
                CODE_EXECUTION_ERROR.format(e.message)
            }
        }
    }
}