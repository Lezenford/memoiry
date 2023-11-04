package com.lezenford.telegram.memoirybot.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("hint")
data class Hint(
    @Column("user_id")
    val userId: Long,

    @Column("key")
    var key: String,

    @Column("value")
    var value: String,

    @Column("type")
    var type: Type
) : Persistable<Int> {
    @Id
    @Column("id")
    var id: Int = 0

    override fun getId(): Int = id

    override fun isNew(): Boolean = id == 0

    enum class Type {
        TEXT
    }
}