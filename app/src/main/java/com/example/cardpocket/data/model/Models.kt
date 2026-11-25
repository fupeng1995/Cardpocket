package com.example.cardpocket.data.model

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

/** 名片。字段值统一放在 card_values（按字段 key 存），这里只保留列表/筛选用的反范式列。 */
data class Card(
    val id: Long,
    val templateId: Long = 0L,
    val avatarEmoji: String = "",
    val name: String = "",
    /** 副标题（前 1–2 个文本字段拼接，保存时计算）。 */
    val subtitle: String = "",
    val relationship: String = "",
    val meetingDate: Long? = null,
    val lastContact: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/** 名片字段模板（仅名称与元信息；字段定义在 template_fields）。 */
data class Template(
    val id: Long,
    val name: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)

/** 模板里的一个字段（每个模板独立副本）。 */
data class TemplateField(
    val id: Long,
    val templateId: Long,
    /** 稳定身份：name / relationship / meeting_date / last_contact / f_xxx。 */
    val key: String,
    val name: String,
    val type: FieldType = FieldType.TEXT,
    val options: List<String> = emptyList(),
    val required: Boolean = false,
    val sortOrder: Int = 0
)

/** 名片快照里的一个字段（建卡时从模板复制）。 */
data class CardField(
    val cardId: Long = 0L,
    val key: String,
    val name: String,
    val type: FieldType = FieldType.TEXT,
    val options: List<String> = emptyList(),
    val required: Boolean = false,
    val sortOrder: Int = 0
)

/** 全局关系标签（由「关系标签管理」统一维护）。 */
data class RelationshipTag(
    val id: Long,
    val name: String,
    val sortOrder: Int = 0
)
