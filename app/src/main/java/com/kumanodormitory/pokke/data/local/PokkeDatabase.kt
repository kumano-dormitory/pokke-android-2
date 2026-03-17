package com.kumanodormitory.pokke.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kumanodormitory.pokke.data.local.converter.Converters
import com.kumanodormitory.pokke.data.local.dao.DutyPersonDao
import com.kumanodormitory.pokke.data.local.dao.OperationLogDao
import com.kumanodormitory.pokke.data.local.dao.ParcelDao
import com.kumanodormitory.pokke.data.local.dao.RyoseiDao
import com.kumanodormitory.pokke.data.local.entity.DutyPersonEntity
import com.kumanodormitory.pokke.data.local.entity.OperationLogEntity
import com.kumanodormitory.pokke.data.local.entity.ParcelEntity
import com.kumanodormitory.pokke.data.local.entity.RyoseiEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        RyoseiEntity::class,
        ParcelEntity::class,
        DutyPersonEntity::class,
        OperationLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PokkeDatabase : RoomDatabase() {
    abstract fun ryoseiDao(): RyoseiDao
    abstract fun parcelDao(): ParcelDao
    abstract fun dutyPersonDao(): DutyPersonDao
    abstract fun operationLogDao(): OperationLogDao

    companion object {
        @Volatile
        private var INSTANCE: PokkeDatabase? = null

        fun getInstance(context: Context): PokkeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PokkeDatabase::class.java,
                    "pokke.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Executors.newSingleThreadExecutor().execute {
                seedDummyData(db)
            }
        }

        private fun seedDummyData(db: SupportSQLiteDatabase) {
            // ── 寮生データ（約30名） ──
            val ryoseiData = listOf(
                // A棟 A1ブロック
                RyoseiSeed("r001", "山田太郎", "やまだたろう", "Yamada Taro", "A101", "A1"),
                RyoseiSeed("r002", "佐藤花子", "さとうはなこ", "Sato Hanako", "A102", "A1"),
                RyoseiSeed("r003", "鈴木一郎", "すずきいちろう", "Suzuki Ichiro", "A103", "A1"),
                // A棟 A2ブロック
                RyoseiSeed("r004", "高橋健太", "たかはしけんた", "Takahashi Kenta", "A201", "A2"),
                RyoseiSeed("r005", "田中美咲", "たなかみさき", "Tanaka Misaki", "A202", "A2"),
                RyoseiSeed("r006", "伊藤大輔", "いとうだいすけ", "Ito Daisuke", "A203", "A2"),
                // A棟 A3ブロック
                RyoseiSeed("r007", "渡辺直人", "わたなべなおと", "Watanabe Naoto", "A301", "A3"),
                RyoseiSeed("r008", "中村さくら", "なかむらさくら", "Nakamura Sakura", "A302", "A3"),
                // A棟 A4ブロック
                RyoseiSeed("r009", "小林誠", "こばやしまこと", "Kobayashi Makoto", "A401", "A4"),
                RyoseiSeed("r010", "加藤雅子", "かとうまさこ", "Kato Masako", "A402", "A4"),
                RyoseiSeed("r011", "吉田拓也", "よしだたくや", "Yoshida Takuya", "A403", "A4"),
                // B棟 B12ブロック
                RyoseiSeed("r012", "松本優", "まつもとゆう", "Matsumoto Yu", "B121", "B12"),
                RyoseiSeed("r013", "井上真理", "いのうえまり", "Inoue Mari", "B122", "B12"),
                RyoseiSeed("r014", "木村翔太", "きむらしょうた", "Kimura Shota", "B123", "B12"),
                // B棟 B3ブロック
                RyoseiSeed("r015", "林恵美", "はやしえみ", "Hayashi Emi", "B301", "B3"),
                RyoseiSeed("r016", "清水圭介", "しみずけいすけ", "Shimizu Keisuke", "B302", "B3"),
                RyoseiSeed("r017", "山口亮", "やまぐちりょう", "Yamaguchi Ryo", "B303", "B3"),
                // B棟 B4ブロック
                RyoseiSeed("r018", "阿部彩", "あべあや", "Abe Aya", "B401", "B4"),
                RyoseiSeed("r019", "石川大地", "いしかわだいち", "Ishikawa Daichi", "B402", "B4"),
                // C棟 C12ブロック
                RyoseiSeed("r020", "前田陽子", "まえだようこ", "Maeda Yoko", "C121", "C12"),
                RyoseiSeed("r021", "藤田健一", "ふじたけんいち", "Fujita Kenichi", "C122", "C12"),
                RyoseiSeed("r022", "岡田真由", "おかだまゆ", "Okada Mayu", "C123", "C12"),
                // C棟 C34ブロック
                RyoseiSeed("r023", "後藤隆", "ごとうたかし", "Goto Takashi", "C341", "C34"),
                RyoseiSeed("r024", "村上愛", "むらかみあい", "Murakami Ai", "C342", "C34"),
                RyoseiSeed("r025", "近藤勇太", "こんどうゆうた", "Kondo Yuta", "C343", "C34"),
                // 臨キャパ
                RyoseiSeed("r026", "遠藤翼", "えんどうつばさ", "Endo Tsubasa", "R101", "臨キャパ"),
                RyoseiSeed("r027", "青木麻衣", "あおきまい", "Aoki Mai", "R102", "臨キャパ"),
                RyoseiSeed("r028", "西村和也", "にしむらかずや", "Nishimura Kazuya", "R103", "臨キャパ"),
                RyoseiSeed("r029", "福田桃子", "ふくだももこ", "Fukuda Momoko", "R104", "臨キャパ"),
                RyoseiSeed("r030", "太田光", "おおたひかる", "Ota Hikaru", "R105", "臨キャパ")
            )

            for (r in ryoseiData) {
                db.execSQL(
                    """
                    INSERT INTO ryosei (id, name, name_kana, name_alphabet, room, block, leaving_date, discord_status)
                    VALUES (?, ?, ?, ?, ?, ?, NULL, 'UNLINKED')
                    """.trimIndent(),
                    arrayOf(r.id, r.name, r.nameKana, r.nameAlphabet, r.room, r.block)
                )
            }

            // ── 事務当番の初期データ ──
            val now = System.currentTimeMillis()
            db.execSQL(
                """
                INSERT INTO duty_person (id, name, updated_at)
                VALUES (?, ?, ?)
                """.trimIndent(),
                arrayOf("dp001", "田中一郎", now)
            )
        }
    }
}

private data class RyoseiSeed(
    val id: String,
    val name: String,
    val nameKana: String,
    val nameAlphabet: String,
    val room: String,
    val block: String
)
