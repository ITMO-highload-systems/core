package org.example.notion.app.paragraph.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("docker")
class DockerConfig(val image: String)