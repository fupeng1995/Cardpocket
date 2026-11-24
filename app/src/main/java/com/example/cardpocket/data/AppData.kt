package com.example.cardpocket.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cardpocket.data.model.Card
import com.example.cardpocket.data.model.ContactRecord
import com.example.cardpocket.data.model.FieldDef
import com.example.cardpocket.data.model.FieldType
import com.example.cardpocket.data.model.FieldValue
import com.example.cardpocket.data.model.Template
import com.example.cardpocket.data.model.OPTION_SEPARATOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private const val DB_NAME = "card_pocket.db"
private const val DB_VERSION = 5

private class CardDbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                nickname TEXT NOT NULL DEFAULT '',
                avatar_emoji TEXT NOT NULL DEFAULT '',
                job_title TEXT NOT NULL DEFAULT '',
                company TEXT NOT NULL DEFAULT '',
                city TEXT NOT NULL DEFAULT '',
                relationship TEXT NOT NULL DEFAULT '',
                meeting_date INTEGER,
                note TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE field_defs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                options TEXT NOT NULL DEFAULT '',
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE field_values (
                card_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                value TEXT NOT NULL DEFAULT '',
                PRIMARY KEY (card_id, field_id)
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
            CREATE TABLE templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL DEFAULT '',
                fields TEXT NOT NULL DEFAULT '',
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE cards ADD COLUMN job_title TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE cards ADD COLUMN company TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE cards ADD COLUMN city TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) {
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
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE records ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 5) {
            db.execSQL(
                """
                CREATE TABLE templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL DEFAULT '',
                    fields TEXT NOT NULL DEFAULT '',
                    sort_order INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

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
            put("name", card.name)
            put("nickname", card.nickname)
            put("avatar_emoji", card.avatarEmoji)
            put("job_title", card.jobTitle)
            put("company", card.company)
            put("city", card.city)
            put("relationship", card.relationship)
            if (card.meetingDate == null) putNull("meeting_date")
            else put("meeting_date", card.meetingDate)
            put("note", card.note)
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
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("records", "card_id = ?", arrayOf(id.toString()))
            db.delete("field_values", "card_id = ?", arrayOf(id.toString()))
            db.delete("cards", "id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllFieldDefs(): List<FieldDef> {
        val out = mutableListOf<FieldDef>()
        readableDatabase.rawQuery(
            "SELECT * FROM field_defs ORDER BY sort_order ASC, id ASC", null
        ).use { c ->
            while (c.moveToNext()) out += c.toFieldDef()
        }
        return out
    }

    fun getFieldDef(id: Long): FieldDef? {
        readableDatabase.rawQuery(
            "SELECT * FROM field_defs WHERE id = ?", arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) c.toFieldDef() else null
        }
    }

    fun upsertFieldDef(def: FieldDef): Long {
        val values = ContentValues().apply {
            put("name", def.name)
            put("type", def.type.name)
            put("options", def.options.joinToString(OPTION_SEPARATOR))
            put("sort_order", def.sortOrder)
        }
        return if (def.id == 0L) {
            writableDatabase.insertOrThrow("field_defs", null, values)
        } else {
            writableDatabase.update("field_defs", values, "id = ?", arrayOf(def.id.toString()))
            def.id
        }
    }

    fun deleteFieldDef(id: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("field_values", "field_id = ?", arrayOf(id.toString()))
            db.delete("field_defs", "id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun moveFieldDef(id: Long, up: Boolean) {
        val all = getAllFieldDefs()
        val index = all.indexOfFirst { it.id == id }
        if (index < 0) return
        val target = if (up) index - 1 else index + 1
        if (target !in all.indices) return
        val a = all[index]
        val b = all[target]
        val db = writableDatabase
        db.execSQL("UPDATE field_defs SET sort_order = ? WHERE id = ?", arrayOf(b.sortOrder, a.id))
        db.execSQL("UPDATE field_defs SET sort_order = ? WHERE id = ?", arrayOf(a.sortOrder, b.id))
    }

    fun getFieldValues(cardId: Long): List<FieldValue> {
        val out = mutableListOf<FieldValue>()
        readableDatabase.rawQuery(
            "SELECT * FROM field_values WHERE card_id = ?", arrayOf(cardId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out += FieldValue(
                    cardId = c.getLong(c.getColumnIndexOrThrow("card_id")),
                    fieldId = c.getLong(c.getColumnIndexOrThrow("field_id")),
                    value = c.getString(c.getColumnIndexOrThrow("value"))
                )
            }
        }
        return out
    }

    fun saveFieldValues(cardId: Long, values: Map<Long, String>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            values.forEach { (fieldId, value) ->
                val cv = ContentValues().apply {
                    put("card_id", cardId)
                    put("field_id", fieldId)
                    put("value", value)
                }
                db.insertWithOnConflict("field_values", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getRecords(cardId: Long): List<ContactRecord> {
        val out = mutableListOf<ContactRecord>()
        readableDatabase.rawQuery(
            "SELECT * FROM records WHERE card_id = ? ORDER BY date_millis DESC, id DESC",
            arrayOf(cardId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out += ContactRecord(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    cardId = c.getLong(c.getColumnIndexOrThrow("card_id")),
                    dateMillis = c.getLong(c.getColumnIndexOrThrow("date_millis")),
                    createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                    type = c.getString(c.getColumnIndexOrThrow("type")),
                    timeText = c.getString(c.getColumnIndexOrThrow("time_text")),
                    content = c.getString(c.getColumnIndexOrThrow("content"))
                )
            }
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

    fun getTemplates(): List<Template> {
        val out = mutableListOf<Template>()
        readableDatabase.rawQuery(
            "SELECT * FROM templates ORDER BY sort_order ASC, id ASC", null
        ).use { c ->
            while (c.moveToNext()) out += c.toTemplate()
        }
        return out
    }

    fun getTemplate(id: Long): Template? {
        readableDatabase.rawQuery(
            "SELECT * FROM templates WHERE id = ?", arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) c.toTemplate() else null
        }
    }

    fun upsertTemplate(template: Template): Long {
        val values = ContentValues().apply {
            put("name", template.name)
            put("fields", template.fields.joinToString(OPTION_SEPARATOR))
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
        writableDatabase.delete("templates", "id = ?", arrayOf(id.toString()))
    }

    private fun Cursor.toCard(): Card = Card(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        nickname = getString(getColumnIndexOrThrow("nickname")),
        avatarEmoji = getString(getColumnIndexOrThrow("avatar_emoji")),
        jobTitle = getString(getColumnIndexOrThrow("job_title")),
        company = getString(getColumnIndexOrThrow("company")),
        city = getString(getColumnIndexOrThrow("city")),
        relationship = getString(getColumnIndexOrThrow("relationship")),
        meetingDate = if (isNull(getColumnIndexOrThrow("meeting_date"))) null
        else getLong(getColumnIndexOrThrow("meeting_date")),
        note = getString(getColumnIndexOrThrow("note")),
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        updatedAt = getLong(getColumnIndexOrThrow("updated_at"))
    )

    private fun Cursor.toFieldDef(): FieldDef = FieldDef(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        type = FieldType.fromName(getString(getColumnIndexOrThrow("type"))),
        options = getString(getColumnIndexOrThrow("options")).let {
            if (it.isEmpty()) emptyList() else it.split(OPTION_SEPARATOR)
        },
        sortOrder = getInt(getColumnIndexOrThrow("sort_order"))
    )

    private fun Cursor.toTemplate(): Template = Template(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        fields = getString(getColumnIndexOrThrow("fields")).let {
            if (it.isEmpty()) emptyList() else it.split(OPTION_SEPARATOR)
        },
        sortOrder = getInt(getColumnIndexOrThrow("sort_order"))
    )
}

/** 全局数据仓库：SQLite 存储 + 内存 StateFlow 供 UI 观察。 */
object AppData {
    private lateinit var db: CardDbHelper

    val cards = MutableStateFlow<List<Card>>(emptyList())
    val fieldDefs = MutableStateFlow<List<FieldDef>>(emptyList())
    val templates = MutableStateFlow<List<Template>>(emptyList())

    fun init(context: Context) {
        if (!::db.isInitialized) db = CardDbHelper(context.applicationContext)
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        cards.value = db.getAllCards()
        fieldDefs.value = db.getAllFieldDefs()
    }

    suspend fun getCard(id: Long): Card? = withContext(Dispatchers.IO) { db.getCard(id) }

    suspend fun getFieldDef(id: Long): FieldDef? =
        withContext(Dispatchers.IO) { db.getFieldDef(id) }

    suspend fun getFieldValues(cardId: Long): List<FieldValue> =
        withContext(Dispatchers.IO) { db.getFieldValues(cardId) }

    suspend fun getRecords(cardId: Long): List<ContactRecord> =
        withContext(Dispatchers.IO) { db.getRecords(cardId) }

    suspend fun saveRecord(record: ContactRecord) =
        withContext(Dispatchers.IO) {
            val toSave = if (record.id == 0L && record.createdAt == 0L) {
                record.copy(createdAt = System.currentTimeMillis())
            } else {
                record
            }
            db.upsertRecord(toSave)
        }

    suspend fun deleteRecord(id: Long) =
        withContext(Dispatchers.IO) { db.deleteRecord(id) }

    suspend fun refreshTemplates() = withContext(Dispatchers.IO) {
        templates.value = db.getTemplates()
    }

    suspend fun getTemplate(id: Long): Template? =
        withContext(Dispatchers.IO) { db.getTemplate(id) }

    suspend fun ensureDefaultTemplates() = withContext(Dispatchers.IO) {
        if (db.getTemplates().isEmpty()) {
            db.upsertTemplate(
                Template(
                    id = 0L,
                    name = "商务伙伴",
                    fields = listOf("姓名", "职位", "公司", "城市", "关系标签", "关系备注"),
                    sortOrder = 0
                )
            )
            db.upsertTemplate(
                Template(
                    id = 0L,
                    name = "朋友与生活",
                    fields = listOf("姓名", "城市", "生日", "兴趣爱好", "关系备注"),
                    sortOrder = 1
                )
            )
        }
        templates.value = db.getTemplates()
    }

    suspend fun saveTemplate(template: Template) = withContext(Dispatchers.IO) {
        val toSave = if (template.id == 0L) {
            val next = db.getTemplates().maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
            template.copy(sortOrder = next)
        } else {
            val existing = db.getTemplates().firstOrNull { it.id == template.id }
            template.copy(sortOrder = existing?.sortOrder ?: 0)
        }
        db.upsertTemplate(toSave)
        templates.value = db.getTemplates()
    }

    suspend fun deleteTemplate(id: Long) = withContext(Dispatchers.IO) {
        db.deleteTemplate(id)
        templates.value = db.getTemplates()
    }

    suspend fun saveCard(card: Card, values: Map<Long, String>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = if (card.id != 0L) db.getCard(card.id) else null
        val toSave = card.copy(createdAt = existing?.createdAt ?: now, updatedAt = now)
        val id = db.upsertCard(toSave)
        db.saveFieldValues(id, values)
        cards.value = db.getAllCards()
    }

    suspend fun deleteCard(id: Long) = withContext(Dispatchers.IO) {
        db.deleteCard(id)
        cards.value = db.getAllCards()
    }

    suspend fun saveFieldDef(def: FieldDef) = withContext(Dispatchers.IO) {
        val toSave = if (def.id == 0L) {
            val next = db.getAllFieldDefs().maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
            def.copy(sortOrder = next)
        } else {
            val existing = db.getFieldDef(def.id)
            def.copy(sortOrder = existing?.sortOrder ?: 0)
        }
        db.upsertFieldDef(toSave)
        fieldDefs.value = db.getAllFieldDefs()
    }

    suspend fun deleteFieldDef(id: Long) = withContext(Dispatchers.IO) {
        db.deleteFieldDef(id)
        fieldDefs.value = db.getAllFieldDefs()
        cards.value = db.getAllCards()
    }

    suspend fun moveFieldDef(id: Long, up: Boolean) = withContext(Dispatchers.IO) {
        db.moveFieldDef(id, up)
        fieldDefs.value = db.getAllFieldDefs()
    }
}
