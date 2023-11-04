package com.lezenford.telegram.memoirybot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MemoiryBotApplication

fun main(args: Array<String>) {
	runApplication<MemoiryBotApplication>(*args)
}
