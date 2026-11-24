package com.example.cardpocket.data.model

/** 用于分隔多选项与单选/多选字段"选项列表"的存储分隔符。 */
const val OPTION_SEPARATOR = "\u001F"

/** 自定义字段类型。 */
enum class FieldType(val label: String) {
    TEXT("单行文本"),
    MULTILINE_TEXT("多行文本"),
    NUMBER("数字"),
    DATE("日期"),
    SWITCH("开关"),
    LINK("链接"),
    SINGLE_SELECT("单选"),
    MULTI_SELECT("多选");

    /** 该类型是否需要维护"选项"列表。 */
    val needsOptions: Boolean
        get() = this == SINGLE_SELECT || this == MULTI_SELECT

    companion object {
        fun fromName(name: String): FieldType =
            entries.firstOrNull { it.name == name } ?: TEXT
    }
}
