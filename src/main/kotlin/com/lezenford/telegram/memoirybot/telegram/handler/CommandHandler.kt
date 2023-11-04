package com.lezenford.telegram.memoirybot.telegram.handler

import com.lezenford.telegram.memoirybot.telegram.Bot
import com.lezenford.telegram.memoirybot.telegram.command.Command
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand

@Component
class CommandHandler(
    commands: List<Command<*>>
) {
    private val commands: Map<String, Command<*>> = commands.associateBy { it.command }

    suspend fun handle(update: Update): BotApiMethod<*>? {
        val message = update.message ?: update.channelPost ?: update.editedMessage ?: update.editedChannelPost
        val command = message.text.takeWhile { it != ' ' }.drop(1).lowercase()
        return commands[command]?.action(update)
    }

    suspend fun publishCommands(bot: Bot){
        commands.values.filter { it.publish }.map {
            BotCommand(it.command, it.description)
        }.takeIf { it.isNotEmpty() }?.also {
            bot.sendMessage(SetMyCommands.builder().commands(it).build())
        }
    }
}