package com.example.cardpocket.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cardpocket.data.model.Card
import com.example.cardpocket.data.model.CardField
import com.example.cardpocket.data.model.ContactRecord
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.OPTION_SEPARATOR
import com.example.cardpocket.data.model.RelationshipTag
import com.example.cardpocket.data.model.Template
import com.example.cardpocket.data.model.TemplateField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private const val DB_NAME = "card_pocket.db"
private const val DB_VERSION = 7

/** 保留 key：界面按这些 key 识别“名字 / 关系 / 认识时间 / 最近联系”。 */
const val KEY_NAME = "name"
const val KEY_RELATIONSHIP = "relationship"
const val KEY_MEETING_DATE = "meeting_date"
const val KEY_LAST_CONTACT = "last_contact"

private val DEFAULT_RELATIONSHIPS = listOf(
    "家人", "朋友", "同事", "同学", "老师", "客户", "邻居", "网友", "一面之缘"
)

private fun newFieldKey(): String =
    "f_" + java.util.UUID.randomUUID().toString().replace("-", "").take(10)

private class CardDbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL DEFAULT '',
                is_default INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE template_fields (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                template_id INTEGER NOT NULL,
                field_key TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL DEFAULT '',
                type TEXT NOT NULL DEFAULT 'TEXT',
                options TEXT NOT NULL DEFAULT '',
                required INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                template_id INTEGER NOT NULL DEFAULT 0,
                avatar_emoji TEXT NOT NULL DEFAULT '',
                name TEXT NOT NULL DEFAULT '',
                subtitle TEXT NOT NULL DEFAULT '',
                relationship TEXT NOT NULL DEFAULT '',
                meeting_date INTEGER,
                last_contact INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE card_fields (
                card_id INTEGER NOT NULL,
                field_key TEXT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                type TEXT NOT NULL DEFAULT 'TEXT',
                options TEXT NOT NULL DEFAULT '',
                required INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (card_id, field_key)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE card_values (
                card_id INTEGER NOT NULL,
                field_key TEXT NOT NULL,
                value TEXT NOT NULL DEFAULT '',
                PRIMARY KEY (card_id, field_key)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                card_id INTEGER NOT NULL,
                date_millis INTEGER NOT NULL,
                created_at INTEGER NOT NULL DEFAULT 0,
                type TEXT NOT NULL DEFAULT '',
                time_text TEXT NOT NULL DEFAULT '',
                content TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE relationship_tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL DEFAULT '',
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        seedDefaultTags(db)
        seedDefaultTemplate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            // 开发测试阶段：旧数据可清空，直接重建表
            db.execSQL("DROP TABLE IF EXISTS cards")
            db.execSQL("DROP TABLE IF EXISTS field_defs")
            db.execSQL("DROP TABLE IF EXISTS field_values")
            db.execSQL("DROP TABLE IF EXISTS records")
            db.execSQL("DROP TABLE IF EXISTS templates")
            db.execSQL("DROP TABLE IF EXISTS template_fields")
            db.execSQL("DROP TABLE IF EXISTS card_fields")
            db.execSQL("DROP TABLE IF EXISTS card_values")
            onCreate(db)
            return
        }
        if (oldVersion < 7) {
            db.execSQL(
                """
                CREATE TABLE relationship_tags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL DEFAULT '',
                    sort_order INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            seedDefaultTags(db)
        }
    }

    fun seedDefaultTemplateIfEmpty() {
        val count = readableDatabase.rawQuery("SELECT COUNT(*) FROM templates", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
        if (count == 0) seedDefaultTemplate(writableDatabase)
    }

    private fun seedDefaultTemplate(db: SQLiteDatabase) {
        val templateId = db.insertOrThrow(
            "templates", null,
            ContentValues().apply {
                put("name", "默认模板")
                put("is_default", 1)
                put("sort_order", 0)
            }
        )
        val fields = listOf(
            Triple(KEY_NAME, "名字", FieldType.TEXT) to true,
            Triple("job_title", "职位", FieldType.TEXT) to false,
            Triple("company", "公司", FieldType.TEXT) to false,
            Triple("native_place", "籍贯", FieldType.TEXT) to false,
            Triple("birthday", "生日", FieldType.DATE) to false,
            Triple(KEY_RELATIONSHIP, "关系标签", FieldType.SINGLE_SELECT) to false,
            Triple(KEY_MEETING_DATE, "认识时间", FieldType.DATE) to false,
            Triple(KEY_LAST_CONTACT, "最近联系时间", FieldType.DATE) to false
        )
        fields.forEachIndexed { index, (triple, required) ->
            val (key, name, type) = triple
            insertField(
                db, templateId, key, name, type,
                if (type == FieldType.SINGLE_SELECT) DEFAULT_RELATIONSHIPS else emptyList(),
                required, index
            )
        }
    }

    private fun seedDefaultTags(db: SQLiteDatabase) {
        DEFAULT_RELATIONSHIPS.forEachIndexed { index, name ->
            db.insertOrThrow(
                "relationship_tags", null,
                ContentValues().apply {
                    put("name", name)
                    put("sort_order", index)
                }
            )
        }
    }

    private fun insertField(
        db: SQLiteDatabase,
        templateId: Long,
        key: String,
        name: String,
        type: FieldType,
        options: List<String>,
        required: Boolean,
        sortOrder: Int
    ) {
        db.insertOrThrow(
            "template_fields", null,
            ContentValues().apply {
                put("template_id", templateId)
                put("field_key", key)
                put("name", name)
                put("type", type.name)
                put("options", options.joinToString(OPTION_SEPARATOR))
                put("required", if (required) 1 else 0)
                put("sort_order", sortOrder)
            }
        )
    }

    // ---------- cards ----------

    fun getAllCards(): List<Card> {
        val out = mutableListOf<Card>()
        readableDatabase.rawQuery("SELECT * FROM cards", null).use { c ->
            while (c.moveToNext()) out += c.toCard()
        }
        return out
    }

    fun getCard(id: Long): Card? {
        readableDatabase.rawQuery("SELECT * FROM cards WHERE id = ?", arrayOf(id.toString())).use { c ->
            return if (c.moveToFirst()) c.toCard() else null
        }
    }

    fun upsertCard(card: Card): Long {
        val values = ContentValues().apply {
            put("template_id", card.templateId)
            put("avatar_emoji", card.avatarEmoji)
            put("name", card.name)
            put("subtitle", card.subtitle)
            put("relationship", card.relationship)
            if (card.meetingDate == null) putNull("meeting_date") else put("meeting_date", card.meetingDate)
            if (card.lastContact == null) putNull("last_contact") else put("last_contact", card.lastContact)
            put("created_at", card.createdAt)
            put("updated_at", card.updatedAt)
        }
        return if (card.id == 0L) {
            writableDatabase.insertOrThrow("cards", null, values)
        } else {
            writableDatabase.update("cards", values, "id = ?", arrayOf(card.id.toString()))
            card.id
        }
    }

    fun deleteCard(id: Long) {
        writableDatabase.delete("card_fields", "card_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("card_values", "card_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("records", "card_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("cards", "id = ?", arrayOf(id.toString()))
    }

    fun updateCardLastContact(cardId: Long, lastContact: Long?, now: Long) {
        val values = ContentValues().apply {
            if (lastContact == null) putNull("last_contact") else put("last_contact", lastContact)
            put("updated_at", now)
        }
        writableDatabase.update("cards", values, "id = ?", arrayOf(cardId.toString()))
    }

    // ---------- card_fields / card_values ----------

    fun getCardFields(cardId: Long): List<CardField> {
        val out = mutableListOf<CardField>()
        readableDatabase.rawQuery(
            "SELECT * FROM card_fields WHERE card_id = ? ORDER BY sort_order ASC",
            arrayOf(cardId.toString())
        ).use { c ->
            while (c.moveToNext()) out += c.toCardField()
        }
        return out
    }

    fun saveCardFields(cardId: Long, fields: List<CardField>) {
        writableDatabase.delete("card_fields", "card_id = ?", arrayOf(cardId.toString()))
        fields.forEach { f ->
            writableDatabase.insertOrThrow(
                "card_fields", null,
                ContentValues().apply {
                    put("card_id", cardId)
                    put("field_key", f.key)
                    put("name", f.name)
                    put("type", f.type.name)
                    put("options", f.options.joinToString(OPTION_SEPARATOR))
                    put("required", if (f.required) 1 else 0)
                    put("sort_order", f.sortOrder)
                }
            )
        }
    }

    fun getCardValues(cardId: Long): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        readableDatabase.rawQuery(
            "SELECT field_key, value FROM card_values WHERE card_id = ?",
            arrayOf(cardId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out[c.getString(c.getColumnIndexOrThrow("field_key"))] =
                    c.getString(c.getColumnIndexOrThrow("value"))
            }
        }
        return out
    }

    fun saveCardValues(cardId: Long, values: Map<String, String>) {
        writableDatabase.delete("card_values", "card_id = ?", arrayOf(cardId.toString()))
        values.forEach { (key, value) ->
            writableDatabase.insertOrThrow(
                "card_values", null,
                ContentValues().apply {
                    put("card_id", cardId)
                    put("field_key", key)
                    put("value", value)
                }
            )
        }
    }

    // ---------- templates ----------

    fun getAllTemplates(): List<Template> {
        val out = mutableListOf<Template>()
        readableDatabase.rawQuery(
            "SELECT * FROM templates ORDER BY is_default DESC, sort_order ASC, id ASC", null
        ).use { c ->
            while (c.moveToNext()) out += c.toTemplate()
        }
        return out
    }

    fun getTemplate(id: Long): Template? {
        readableDatabase.rawQuery("SELECT * FROM templates WHERE id = ?", arrayOf(id.toString())).use { c ->
            return if (c.moveToFirst()) c.toTemplate() else null
        }
    }

    fun upsertTemplate(template: Template): Long {
        val values = ContentValues().apply {
            put("name", template.name)
            put("is_default", if (template.isDefault) 1 else 0)
            put("sort_order", template.sortOrder)
        }
        return if (template.id == 0L) {
            writableDatabase.insertOrThrow("templates", null, values)
        } else {
            writableDatabase.update("templates", values, "id = ?", arrayOf(template.id.toString()))
            template.id
        }
    }

    fun deleteTemplate(id: Long) {
        writableDatabase.delete("template_fields", "template_id = ?", arrayOf(id.toString()))
        writableDatabase.delete("templates", "id = ?", arrayOf(id.toString()))
    }

    fun clearDefaultTemplate() {
        writableDatabase.execSQL("UPDATE templates SET is_default = 0")
    }

    fun getTemplateFields(templateId: Long): List<TemplateField> {
        val out = mutableListOf<TemplateField>()
        readableDatabase.rawQuery(
            "SELECT * FROM template_fields WHERE template_id = ? ORDER BY sort_order ASC, id ASC",
            arrayOf(templateId.toString())
        ).use { c ->
            while (c.moveToNext()) out += c.toTemplateField()
        }
        return out
    }

    fun getAllTemplateFields(): List<TemplateField> {
        val out = mutableListOf<TemplateField>()
        readableDatabase.rawQuery(
            "SELECT * FROM template_fields ORDER BY template_id ASC, sort_order ASC, id ASC", null
        ).use { c ->
            while (c.moveToNext()) out += c.toTemplateField()
        }
        return out
    }

    fun replaceTemplateFields(templateId: Long, fields: List<TemplateField>) {
        writableDatabase.delete("template_fields", "template_id = ?", arrayOf(templateId.toString()))
        fields.forEach { f ->
            writableDatabase.insertOrThrow(
                "template_fields", null,
                ContentValues().apply {
                    put("template_id", templateId)
                    put("field_key", f.key)
                    put("name", f.name)
                    put("type", f.type.name)
                    put("options", f.options.joinToString(OPTION_SEPARATOR))
                    put("required", if (f.required) 1 else 0)
                    put("sort_order", f.sortOrder)
                }
            )
        }
    }

    // ---------- relationship tags ----------

    fun getRelationshipTags(): List<RelationshipTag> {
        val out = mutableListOf<RelationshipTag>()
        readableDatabase.rawQuery(
            "SELECT * FROM relationship_tags ORDER BY sort_order ASC, id ASC", null
        ).use { c ->
            while (c.moveToNext()) {
                out += RelationshipTag(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    name = c.getString(c.getColumnIndexOrThrow("name")),
                    sortOrder = c.getInt(c.getColumnIndexOrThrow("sort_order"))
                )
            }
        }
        return out
    }

    fun getMaxTagSortOrder(): Int =
        readableDatabase.rawQuery("SELECT MAX(sort_order) FROM relationship_tags", null).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getInt(0) else -1
        }

    fun insertRelationshipTag(name: String, sortOrder: Int) {
        writableDatabase.insertOrThrow(
            "relationship_tags", null,
            ContentValues().apply {
                put("name", name)
                put("sort_order", sortOrder)
            }
        )
    }

    fun renameRelationshipTagRow(id: Long, newName: String) {
        writableDatabase.update(
            "relationship_tags",
            ContentValues().apply { put("name", newName) },
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteRelationshipTagRow(id: Long) {
        writableDatabase.delete("relationship_tags", "id = ?", arrayOf(id.toString()))
    }

    fun updateTagSortOrder(id: Long, sortOrder: Int) {
        writableDatabase.update(
            "relationship_tags",
            ContentValues().apply { put("sort_order", sortOrder) },
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun countCardsWithRelationship(name: String): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM cards WHERE relationship = ?", arrayOf(name)
        ).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    fun renameRelationshipValue(oldName: String, newName: String) {
        writableDatabase.update(
            "cards",
            ContentValues().apply { put("relationship", newName) },
            "relationship = ?",
            arrayOf(oldName)
        )
        writableDatabase.update(
            "card_values",
            ContentValues().apply { put("value", newName) },
            "field_key = ? AND value = ?",
            arrayOf(KEY_RELATIONSHIP, oldName)
        )
    }

    // ---------- records ----------

    fun getRecords(cardId: Long): List<ContactRecord> {
        val out = mutableListOf<ContactRecord>()
        readableDatabase.rawQuery(
            "SELECT * FROM records WHERE card_id = ? ORDER BY date_millis DESC, id DESC",
            arrayOf(cardId.toString())
        ).use { c ->
            while (c.moveToNext()) out += c.toRecord()
        }
        return out
    }

    fun upsertRecord(record: ContactRecord): Long {
        val values = ContentValues().apply {
            put("card_id", record.cardId)
            put("date_millis", record.dateMillis)
            put("created_at", record.createdAt)
            put("type", record.type)
            put("time_text", record.timeText)
            put("content", record.content)
        }
        return if (record.id == 0L) {
            writableDatabase.insertOrThrow("records", null, values)
        } else {
            writableDatabase.update("records", values, "id = ?", arrayOf(record.id.toString()))
            record.id
        }
    }

    fun deleteRecord(id: Long) {
        writableDatabase.delete("records", "id = ?", arrayOf(id.toString()))
    }

    fun getMaxRecordDate(cardId: Long): Long? {
        readableDatabase.rawQuery(
            "SELECT MAX(date_millis) FROM records WHERE card_id = ?",
            arrayOf(cardId.toString())
        ).use { c ->
            return if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null
        }
    }

    fun getRecordCardId(id: Long): Long? {
        readableDatabase.rawQuery(
            "SELECT card_id FROM records WHERE id = ?", arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) c.getLong(0) else null
        }
    }

    // ---------- mappers ----------

    private fun Cursor.toCard(): Card = Card(
        id = getLong(getColumnIndexOrThrow("id")),
        templateId = getLong(getColumnIndexOrThrow("template_id")),
        avatarEmoji = getString(getColumnIndexOrThrow("avatar_emoji")),
        name = getString(getColumnIndexOrThrow("name")),
        subtitle = getString(getColumnIndexOrThrow("subtitle")),
        relationship = getString(getColumnIndexOrThrow("relationship")),
        meetingDate = if (isNull(getColumnIndexOrThrow("meeting_date"))) null
        else getLong(getColumnIndexOrThrow("meeting_date")),
        lastContact = if (isNull(getColumnIndexOrThrow("last_contact"))) null
        else getLong(getColumnIndexOrThrow("last_contact")),
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        updatedAt = getLong(getColumnIndexOrThrow("updated_at"))
    )

    private fun Cursor.toCardField(): CardField = CardField(
        cardId = getLong(getColumnIndexOrThrow("card_id")),
        key = getString(getColumnIndexOrThrow("field_key")),
        name = getString(getColumnIndexOrThrow("name")),
        type = FieldType.fromName(getString(getColumnIndexOrThrow("type"))),
        options = getString(getColumnIndexOrThrow("options")).let {
            if (it.isEmpty()) emptyList() else it.split(OPTION_SEPARATOR)
        },
        required = getInt(getColumnIndexOrThrow("required")) == 1,
        sortOrder = getInt(getColumnIndexOrThrow("sort_order"))
    )

    private fun Cursor.toTemplate(): Template = Template(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        isDefault = getInt(getColumnIndexOrThrow("is_default")) == 1,
        sortOrder = getInt(getColumnIndexOrThrow("sort_order"))
    )

    private fun Cursor.toTemplateField(): TemplateField = TemplateField(
        id = getLong(getColumnIndexOrThrow("id")),
        templateId = getLong(getColumnIndexOrThrow("template_id")),
        key = getString(getColumnIndexOrThrow("field_key")),
        name = getString(getColumnIndexOrThrow("name")),
        type = FieldType.fromName(getString(getColumnIndexOrThrow("type"))),
        options = getString(getColumnIndexOrThrow("options")).let {
            if (it.isEmpty()) emptyList() else it.split(OPTION_SEPARATOR)
        },
        required = getInt(getColumnIndexOrThrow("required")) == 1,
        sortOrder = getInt(getColumnIndexOrThrow("sort_order"))
    )

    private fun Cursor.toRecord(): ContactRecord = ContactRecord(
        id = getLong(getColumnIndexOrThrow("id")),
        cardId = getLong(getColumnIndexOrThrow("card_id")),
        dateMillis = getLong(getColumnIndexOrThrow("date_millis")),
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        type = getString(getColumnIndexOrThrow("type")),
        timeText = getString(getColumnIndexOrThrow("time_text")),
        content = getString(getColumnIndexOrThrow("content"))
    )
}

/** 全局数据仓库：SQLite 存储 + 内存 StateFlow 供 UI 观察。 */
object AppData {
    private lateinit var db: CardDbHelper

    val cards = MutableStateFlow<List<Card>>(emptyList())
    val templates = MutableStateFlow<List<Template>>(emptyList())
    val relationshipTags = MutableStateFlow<List<RelationshipTag>>(emptyList())

    fun init(context: Context) {
        if (!::db.isInitialized) db = CardDbHelper(context.applicationContext)
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        db.seedDefaultTemplateIfEmpty()
        cards.value = db.getAllCards()
        templates.value = db.getAllTemplates()
        relationshipTags.value = db.getRelationshipTags()
    }

    suspend fun addRelationshipTag(name: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@withContext false
        if (db.getRelationshipTags().any { it.name == trimmed }) return@withContext false
        db.insertRelationshipTag(trimmed, db.getMaxTagSortOrder() + 1)
        relationshipTags.value = db.getRelationshipTags()
        true
    }

    suspend fun renameRelationshipTag(id: Long, newName: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return@withContext false
        val tags = db.getRelationshipTags()
        val target = tags.firstOrNull { it.id == id } ?: return@withContext false
        if (tags.any { it.id != id && it.name == trimmed }) return@withContext false
        db.renameRelationshipTagRow(id, trimmed)
        db.renameRelationshipValue(target.name, trimmed)
        relationshipTags.value = db.getRelationshipTags()
        cards.value = db.getAllCards()
        true
    }

    suspend fun deleteRelationshipTag(id: Long): Boolean = withContext(Dispatchers.IO) {
        val tags = db.getRelationshipTags()
        val target = tags.firstOrNull { it.id == id } ?: return@withContext false
        if (db.countCardsWithRelationship(target.name) > 0) return@withContext false
        db.deleteRelationshipTagRow(id)
        relationshipTags.value = db.getRelationshipTags()
        true
    }

    suspend fun moveRelationshipTag(id: Long, up: Boolean) = withContext(Dispatchers.IO) {
        val tags = db.getRelationshipTags().toMutableList()
        val index = tags.indexOfFirst { it.id == id }
        if (index < 0) return@withContext
        val other = if (up) index - 1 else index + 1
        if (other !in tags.indices) return@withContext
        val a = tags[index]
        val b = tags[other]
        db.updateTagSortOrder(a.id, b.sortOrder)
        db.updateTagSortOrder(b.id, a.sortOrder)
        relationshipTags.value = db.getRelationshipTags()
    }

    suspend fun reorderRelationshipTags(from: Int, to: Int) = withContext(Dispatchers.IO) {
        val tags = db.getRelationshipTags().toMutableList()
        if (from !in tags.indices || to !in tags.indices || from == to) return@withContext
        val item = tags.removeAt(from)
        tags.add(to, item)
        tags.forEachIndexed { i, t -> db.updateTagSortOrder(t.id, i) }
        relationshipTags.value = db.getRelationshipTags()
    }

    suspend fun getCard(id: Long): Card? = withContext(Dispatchers.IO) { db.getCard(id) }

    suspend fun getCardFields(cardId: Long): List<CardField> =
        withContext(Dispatchers.IO) { db.getCardFields(cardId) }

    suspend fun getCardValues(cardId: Long): Map<String, String> =
        withContext(Dispatchers.IO) { db.getCardValues(cardId) }

    suspend fun saveCard(
        cardId: Long,
        templateId: Long,
        avatarEmoji: String,
        fields: List<CardField>,
        values: Map<String, String>
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = if (cardId != 0L) db.getCard(cardId) else null
        val name = values[KEY_NAME].orEmpty()
        val relationship = values[KEY_RELATIONSHIP].orEmpty()
        val meetingDate = values[KEY_MEETING_DATE]?.toLongOrNull()
        val lastContact = values[KEY_LAST_CONTACT]?.toLongOrNull()
        val subtitle = fields
            .filter { it.key != KEY_NAME && (it.type == FieldType.TEXT || it.type == FieldType.MULTILINE_TEXT) }
            .sortedBy { it.sortOrder }
            .mapNotNull { f -> values[f.key]?.takeIf { it.isNotBlank() } }
            .take(2)
            .joinToString(" · ")
        val card = Card(
            id = cardId,
            templateId = templateId,
            avatarEmoji = avatarEmoji,
            name = name,
            subtitle = subtitle,
            relationship = relationship,
            meetingDate = meetingDate,
            lastContact = lastContact,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        val id = db.upsertCard(card)
        db.saveCardFields(id, fields)
        db.saveCardValues(id, values)
        cards.value = db.getAllCards()
        id
    }

    suspend fun deleteCard(id: Long) = withContext(Dispatchers.IO) {
        db.deleteCard(id)
        cards.value = db.getAllCards()
    }

    /** 给单张名片追加一个字段（本卡个性化）。 */
    suspend fun addCardField(cardId: Long, field: CardField) = withContext(Dispatchers.IO) {
        val existingFields = db.getCardFields(cardId)
        if (existingFields.any { it.name == field.name }) return@withContext
        val sortOrder = (existingFields.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val fields = existingFields + field.copy(sortOrder = sortOrder)
        db.saveCardFields(cardId, fields)
        val values = db.getCardValues(cardId).toMutableMap()
        if (!values.containsKey(field.key)) values[field.key] = ""
        db.saveCardValues(cardId, values)
        syncCardColumns(cardId, fields, values)
        cards.value = db.getAllCards()
    }

    private fun syncCardColumns(cardId: Long, fields: List<CardField>, values: Map<String, String>) {
        val card = db.getCard(cardId) ?: return
        val name = values[KEY_NAME].orEmpty()
        val relationship = values[KEY_RELATIONSHIP].orEmpty()
        val meetingDate = values[KEY_MEETING_DATE]?.toLongOrNull()
        val lastContact = values[KEY_LAST_CONTACT]?.toLongOrNull()
        val subtitle = fields
            .filter { it.key != KEY_NAME && (it.type == FieldType.TEXT || it.type == FieldType.MULTILINE_TEXT) }
            .sortedBy { it.sortOrder }
            .mapNotNull { f -> values[f.key]?.takeIf { it.isNotBlank() } }
            .take(2)
            .joinToString(" · ")
        db.upsertCard(
            card.copy(
                name = name,
                subtitle = subtitle,
                relationship = relationship,
                meetingDate = meetingDate,
                lastContact = lastContact,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ---------- templates ----------

    suspend fun getTemplate(id: Long): Template? = withContext(Dispatchers.IO) { db.getTemplate(id) }

    suspend fun getTemplateFields(templateId: Long): List<TemplateField> =
        withContext(Dispatchers.IO) { db.getTemplateFields(templateId) }

    suspend fun getAllTemplateFields(): List<TemplateField> =
        withContext(Dispatchers.IO) { db.getAllTemplateFields() }

    suspend fun saveTemplate(
        templateId: Long,
        name: String,
        isDefault: Boolean,
        fields: List<TemplateField>
    ): Long = withContext(Dispatchers.IO) {
        val existing = if (templateId != 0L) db.getTemplate(templateId) else null
        val nextSort = db.getAllTemplates().maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
        val id = db.upsertTemplate(
            Template(
                id = templateId,
                name = name,
                isDefault = isDefault,
                sortOrder = existing?.sortOrder ?: nextSort
            )
        )
        db.replaceTemplateFields(id, fields.mapIndexed { i, f -> f.copy(sortOrder = i) })
        if (isDefault) {
            db.clearDefaultTemplate()
            db.upsertTemplate(Template(id = id, name = name, isDefault = true, sortOrder = existing?.sortOrder ?: nextSort))
        }
        templates.value = db.getAllTemplates()
        id
    }

    suspend fun deleteTemplate(id: Long) = withContext(Dispatchers.IO) {
        db.deleteTemplate(id)
        templates.value = db.getAllTemplates()
    }

    suspend fun setDefaultTemplate(id: Long) = withContext(Dispatchers.IO) {
        db.clearDefaultTemplate()
        val t = db.getTemplate(id)
        if (t != null) db.upsertTemplate(t.copy(isDefault = true))
        templates.value = db.getAllTemplates()
    }

    // ---------- records ----------

    suspend fun getRecords(cardId: Long): List<ContactRecord> =
        withContext(Dispatchers.IO) { db.getRecords(cardId) }

    suspend fun saveRecord(record: ContactRecord) = withContext(Dispatchers.IO) {
        val toSave = if (record.id == 0L && record.createdAt == 0L) {
            record.copy(createdAt = System.currentTimeMillis())
        } else {
            record
        }
        db.upsertRecord(toSave)
        refreshLastContact(record.cardId)
    }

    suspend fun deleteRecord(id: Long) = withContext(Dispatchers.IO) {
        val cardId = db.getRecordCardId(id)
        db.deleteRecord(id)
        cardId?.let { refreshLastContact(it) }
    }

    private fun refreshLastContact(cardId: Long) {
        val maxDate = db.getMaxRecordDate(cardId)
        val card = db.getCard(cardId) ?: return
        val fields = db.getCardFields(cardId)
        if (fields.any { it.key == KEY_LAST_CONTACT }) {
            val values = db.getCardValues(cardId).toMutableMap()
            if (maxDate == null) values.remove(KEY_LAST_CONTACT) else values[KEY_LAST_CONTACT] = maxDate.toString()
            db.saveCardValues(cardId, values)
        }
        db.updateCardLastContact(cardId, maxDate, System.currentTimeMillis())
        cards.value = db.getAllCards()
    }
}
