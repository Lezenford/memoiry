package com.lezenford.telegram.memoiry

import com.github.kotlintelegrambot.bot
import com.lezenford.telegram.memoiry.bot.init
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import tech.ydb.auth.iam.CloudAuthHelper
import tech.ydb.core.grpc.GrpcTransport
import tech.ydb.core.grpc.GrpcTransportBuilder
import tech.ydb.table.SessionRetryContext
import tech.ydb.table.TableClient
import java.time.Duration

fun main() {
    GrpcTransport.forConnectionString(Properties.connectionString)
        .withAuthProvider(CloudAuthHelper.getAuthProviderFromEnviron())
        .withInitMode(GrpcTransportBuilder.InitMode.ASYNC_FALLBACK)
        .build().use {
            context(
                SessionRetryContext.create(
                    TableClient.newClient(it)
                        .sessionPoolSize(1, 10)
                        .sessionKeepAliveTime(Duration.ofSeconds(10))
                        .build()
                ).build()
            ) {
                context(bot { init() }.apply { startWebhook() }) {
                    embeddedServer(Netty, port = Properties.port, module = { configureRouting() }).start(wait = true)
                }
            }
        }
}

