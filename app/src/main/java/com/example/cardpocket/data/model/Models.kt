package com.example.cardpocket.data.model

/** 名片（一位联系人）。 */
data class Card(
    val id: Long,
    /** 姓名（必填）。 */
    val name: String,
    val nickname: String = "",
    val avatarEmoji: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val city: String = "",
    val relationship: String = "",
    val meetingDate: Long? = null,
    val note: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/** 自定义字段的定义。 */
data class FieldDef(
    val id: Long,
    val name: String,
    val type: FieldType,
    val options: List<String> = emptyList(),
    val sortOrder: Int = 0
)

/** 某张名片对某个自定义字段的取值。 */
data class FieldValue(
    val cardId: Long,
    val fieldId: Long,
    val value: String = ""
)

/** 一条联系记录（联系时间线条目）。 */
data class ContactRecord(
    val id: Long,
    val cardId: Long,
    /** 记录日期（当天 0 点的毫秒时间戳）。 */
    val dateMillis: Long,
    /** 记录的添加时间（毫秒时间戳）。 */
    val createdAt: Long = 0L,
    /** 类型：提醒 / 消息 / 见面 / 电话。 */
    val type: String = "",
    /** 时间文本，例如 10:20。 */
    val timeText: String = "",
    val content: String = ""
)

/** 名片字段模板：定义新建联系人时展示哪些字段、以什么顺序展示。 */
data class Template(
    val id: Long,
    val name: String,
    /** 有序的字段名列表，例如 姓名 / 职位 / 公司。 */
    val fields: List<String> = emptyList(),
    val sortOrder: Int = 0
)
