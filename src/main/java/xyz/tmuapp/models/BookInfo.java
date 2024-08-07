package xyz.tmuapp.models;

import com.google.gson.stream.JsonReader;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import xyz.tmuapp.Main;
import xyz.tmuapp.archive.ArchiveUnpackerFactory;
import xyz.tmuapp.archive.IArchiveUnpacker;
import xyz.tmuapp.managers.LocaleManager;
import xyz.tmuapp.utils.ArrayUtils;
import xyz.tmuapp.utils.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class BookInfo {
    public static final String METADATA_FILE_NAME = "book_info.json";

    private List<Integer> genres;
    private List<String> tags;
    private String description;

    @Nullable
    public static BookInfo findInDirectory(String directory) {
        File bookInfoFile = FileUtils.getAllFilesFromDirectory(directory, new String[]{"json"}, true)
                .stream()
                .filter(file -> file.getName().equalsIgnoreCase(BookInfo.METADATA_FILE_NAME))
                .findAny()
                .orElse(null);

        return Optional.ofNullable(bookInfoFile)
                .filter(FileUtils::isFileExist)
                .map(file -> {
                    try {
                        return new FileReader(bookInfoFile, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .map(BookInfo::readJsonFromReader)
                .orElse(null);
    }

    @Nullable
    public static BookInfo findInArchive(File archiveFile) {
        InputStream is = null;
        IArchiveUnpacker unpacker = ArchiveUnpackerFactory.create(archiveFile);
        try {
            unpacker.open(archiveFile.toString());

            return Optional.ofNullable(is = findBookInfoStreamInArchive(unpacker))
                    .map(stream -> new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .map(BookInfo::readJsonFromReader)
                    .orElse(null);
        } catch (Exception ignored) {
        } finally {
            FileUtils.closeQuietly(is);
            FileUtils.closeQuietly(unpacker);
        }
        return null;
    }

    @Nullable
    private static InputStream findBookInfoStreamInArchive(IArchiveUnpacker unpacker) throws IOException {
        while (unpacker.next()) {
            if (unpacker.getEntryName().toLowerCase().endsWith(METADATA_FILE_NAME)) {
                return unpacker.getEntryInputStream();
            }
        }
        return null;
    }

    @Nullable
    private static BookInfo readJsonFromReader(Reader reader) {
        try (JsonReader jsonReader = new JsonReader(reader)) {
            return Main.GSON.fromJson(jsonReader, BookInfo.class);
        } catch (IOException ignored) {
        } finally {
            FileUtils.closeQuietly(reader);
        }
        return null;
    }

    public List<String> getGenres() {
        if (ArrayUtils.isNotEmpty(genres)) {
            return genres.stream()
                    .map(Genre::getGenreFromInt)
                    .filter(Objects::nonNull)
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .map(name -> "genre_" + name)
                    .map(LocaleManager::getString)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private enum Genre {
        ACTION,
        ADULT,
        ADVENTURE,
        COMEDY,
        DOUJINSHI,
        DRAMA,
        ECCHI,
        FANTASY,
        GENDERBENDER,
        HAREM,
        HISTORICAL,
        HORROR,
        JOSEI,
        MAGIC,
        MARTIALARTS,
        MECHA,
        MYSTERY,
        ONESHOT,
        PSYCHOLOGICAL,
        ROMANCE,
        SCHOOLLIFE,
        SCIFI,
        SEINEN,
        SHOUJO,
        SHOUJOAI,
        SHOUNEN,
        SHOUNENAI,
        SLICEOFLIFE,
        SPORTS,
        SUPERNATURAL,
        TRAGEDY,
        YAOI,
        YURI;

        static final String[] ActionArr;
        static final String[] AdultArr;
        static final String[] AdventureArr;
        static final String[] ComedyArr;
        static final String[] DoujinshiArr;
        static final String[] DramaArr;
        static final String[] EcchiArr;
        static final String[] FantasyArr;
        static final String[] GenderBenderArr;
        static final String[] HaremArr;
        static final String[] HistoricalArr;
        static final String[] HorrorArr;
        static final String[] JoseiArr;
        static final String[] MagicArr;
        static final String[] MartialArtsArr;
        static final String[] MechaArr;
        static final String[] MysteryArr;
        static final String[] OneShotArr;
        static final String[] PsychologicalArr;
        static final String[] RomanceArr;
        static final String[] SchoolLifeArr;
        static final String[] SciFiArr;
        static final String[] SeinenArr;
        static final String[] ShoujoArr;
        static final String[] ShoujoAiArr;
        static final String[] ShounenArr;
        static final String[] ShounenAiArr;
        static final String[] SliceOfLifeArr;
        static final String[] SportsArr;
        static final String[] SupernaturalArr;
        static final String[] TragedyArr;
        static final String[] YaoiArr;
        static final String[] YuriArr;
        static HashMap<Genre, String[]> MangaGenreStr;

        @Nullable
        public static Genre getGenreFromInt(int genreInt) {
            if (genreInt >= 0 && genreInt < Genre.values().length) {
                return Genre.values()[genreInt];
            }

            return null;
        }

        static {
            ActionArr = new String[]{
                    "action",
                    "acción",
                    "live action",
                    "azione",
                    "ação",
                    "حركة",
                    "экшн",
                    "экшен",
                    "боевик",
                    "бойовик"
            };

            AdultArr = new String[]{
                    "adult",
                    "mature",
                    "erotica",
                    "erotic",
                    "adulte",
                    "adulta",
                    "adulto",
                    "maduro",
                    "maduras",
                    "madura",
                    "orgía",
                    "эротика",
                    "18 плюс",
                    "18+",
                    "хентай",
                    "еротика",
                    "для взрослых",
                    "для дорослих"
            };

            AdventureArr = new String[]{
                    "adventure",
                    "aventure",
                    "aventura",
                    "aventuras",
                    "avventura",
                    "مغامرات",
                    "приключения",
                    "пригоди"
            };

            ComedyArr = new String[]{
                    "comedy",
                    "comédie",
                    "comédia",
                    "comedia",
                    "humor",
                    "commedia",
                    "مضحك",
                    "комедия",
                    "комедии",
                    "комедія",
                    "комедії"
            };

            DoujinshiArr = new String[]{
                    "doujinshi",
                    "hentai",
                    "dounshinji",
                    "додзинси"
            };

            DramaArr = new String[]{
                    "drama",
                    "drame",
                    "drammatico",
                    "دراما",
                    "драма"
            };

            EcchiArr = new String[]{
                    "ecchi",
                    "إيتشي",
                    "eichii",
                    "эччи",
                    "этти",
                    "еччі"
            };

            FantasyArr = new String[]{
                    "fantasy",
                    "fantaisie",
                    "fantasía",
                    "خيال",
                    "fantastique",
                    "fantasia",
                    "fantasi",
                    "fantasia negra",
                    "фэнтези",
                    "фэнтази",
                    "героическое фэнтези",
                    "фентезі"
            };

            GenderBenderArr = new String[]{
                    "gender bender",
                    "sexedit",
                    "gender intriga",
                    "travelo",
                    "transexual",
                    "гендерная интрига"
            };

            HaremArr = new String[]{
                    "harem",
                    "harén",
                    "harém",
                    "гарем"
            };

            HistoricalArr = new String[]{
                    "historical",
                    "historic",
                    "historique",
                    "histórica",
                    "histórico",
                    "storico",
                    "تاريخي",
                    "история",
                    "историческое",
                    "історія"
            };

            HorrorArr = new String[]{
                    "horror",
                    "horreur",
                    "رعب",
                    "ужасы",
                    "хоррор",
                    "жахи"
            };

            JoseiArr = new String[]{
                    "josei",
                    "dsesay",
                    "جوسيّ",
                    "дзёсэй"
            };

            MagicArr = new String[]{
                    "magic",
                    "magico",
                    "magia",
                    "магия"
            };

            MartialArtsArr = new String[]{
                    "martial arts",
                    "fightskill",
                    "arts martiaux",
                    "artes marciales",
                    "فنون قتالية",
                    "arte marcial",
                    "artes marciais",
                    "samurai",
                    "боевые искусства",
                    "самурайский боевик",
                    "бойове мистецтво"
            };

            MechaArr = new String[]{
                    "mecha",
                    "меха"
            };

            MysteryArr = new String[]{
                    "mystery",
                    "mystère",
                    "misterio",
                    "mistério",
                    "misteri",
                    "غموض",
                    "мистика",
                    "містика"
            };

            OneShotArr = new String[]{
                    "one shot",
                    "فصل واحد",
                    "ваншот"
            };

            PsychologicalArr = new String[]{
                    "psychological",
                    "psycho",
                    "psicologico",
                    "psicológico",
                    "نفساني",
                    "psychologique",
                    "психология",
                    "психологическое"
            };

            RomanceArr = new String[]{
                    "romance",
                    "romantica",
                    "romantico",
                    "romántica",
                    "romántico",
                    "romãntico",
                    "sentimentale",
                    "رومانسي",
                    "романтика",
                    "шмарклі"
            };

            SchoolLifeArr = new String[]{
                    "school life",
                    "school",
                    "vie scolaire",
                    "escolar",
                    "vida escola",
                    "vida escolar",
                    "scolastico",
                    "حياة مدرسية",
                    "школа"
            };

            SciFiArr = new String[]{
                    "sci fi",
                    "scifi",
                    "fantastic",
                    "science fiction",
                    "ciencia ficción",
                    "رجل آلي",
                    "خيال علمي",
                    "science-fiction",
                    "fantascienza",
                    "ciência ficção",
                    "ficção científica",
                    "научная фантастика",
                    "фантастика"
            };

            SeinenArr = new String[]{
                    "seinen",
                    "سيّنين",
                    "сэйнэн",
                    "сэйнен",
                    "шьонен"
            };

            ShoujoArr = new String[]{
                    "shoujo",
                    "shojo",
                    "shōjo",
                    "شوجو",
                    "shôjo",
                    "сёдзё",
                    "шьоджьо"
            };

            ShoujoAiArr = new String[]{
                    "shoujo ai",
                    "shoujoai",
                    "сёдзё ай",
                    "шьоджьо aї"
            };
            ShounenArr = new String[]{
                    "shounen",
                    "shonen",
                    "shonem",
                    "shōnen",
                    "شونين",
                    "shônen",
                    "сёнэн",
                    "сёнен"
            };

            ShounenAiArr = new String[]{
                    "shounen ai",
                    "shounenai",
                    "сёнэн ай",
                    "сёнен ай",
                    "шьонен aї"
            };

            SliceOfLifeArr = new String[]{
                    "slice of life",
                    "natural",
                    "routine",
                    "tranche de vie",
                    "vida real",
                    "vida cotidiana",
                    "vita quotidiana",
                    "cotidiano",
                    "حركة حياتية",
                    "شريحة من الحياة",
                    "повседневность",
                    "буденність"
            };
            SportsArr = new String[]{
                    "sports",
                    "sport",
                    "deporte",
                    "deportes",
                    "sportivo",
                    "esporte",
                    "esportes",
                    "رياضة",
                    "спорт"
            };

            SupernaturalArr = new String[]{
                    "supernatural",
                    "surnaturel",
                    "sobrenatural",
                    "sobrenarutal",
                    "sovrannaturale",
                    "soprannaturale",
                    "قوى خارقة",
                    "сверхъестественное",
                    "надприродне"
            };

            TragedyArr = new String[]{
                    "tragedy",
                    "tragédie",
                    "tragedia",
                    "tragédia",
                    "tragico",
                    "مأسوي",
                    "трагедия"
            };

            YaoiArr = new String[]{
                    "yaoi",
                    "яой"
            };

            YuriArr = new String[]{
                    "yuri",
                    "юри"
            };

            Genre.MangaGenreStr = new HashMap<>() {{
                put(Genre.ACTION, Genre.ActionArr);
                put(Genre.ADULT, Genre.AdultArr);
                put(Genre.ADVENTURE, Genre.AdventureArr);
                put(Genre.COMEDY, Genre.ComedyArr);
                put(Genre.DOUJINSHI, Genre.DoujinshiArr);
                put(Genre.DRAMA, Genre.DramaArr);
                put(Genre.ECCHI, Genre.EcchiArr);
                put(Genre.FANTASY, Genre.FantasyArr);
                put(Genre.GENDERBENDER, Genre.GenderBenderArr);
                put(Genre.HAREM, Genre.HaremArr);
                put(Genre.HISTORICAL, Genre.HistoricalArr);
                put(Genre.HORROR, Genre.HorrorArr);
                put(Genre.JOSEI, Genre.JoseiArr);
                put(Genre.MAGIC, Genre.MagicArr);
                put(Genre.MARTIALARTS, Genre.MartialArtsArr);
                put(Genre.MECHA, Genre.MechaArr);
                put(Genre.MYSTERY, Genre.MysteryArr);
                put(Genre.ONESHOT, Genre.OneShotArr);
                put(Genre.PSYCHOLOGICAL, Genre.PsychologicalArr);
                put(Genre.ROMANCE, Genre.RomanceArr);
                put(Genre.SCHOOLLIFE, Genre.SchoolLifeArr);
                put(Genre.SCIFI, Genre.SciFiArr);
                put(Genre.SEINEN, Genre.SeinenArr);
                put(Genre.SHOUJO, Genre.ShoujoArr);
                put(Genre.SHOUJOAI, Genre.ShoujoAiArr);
                put(Genre.SHOUNEN, Genre.ShounenArr);
                put(Genre.SHOUNENAI, Genre.ShounenAiArr);
                put(Genre.SLICEOFLIFE, Genre.SliceOfLifeArr);
                put(Genre.SPORTS, Genre.SportsArr);
                put(Genre.SUPERNATURAL, Genre.SupernaturalArr);
                put(Genre.TRAGEDY, Genre.TragedyArr);
                put(Genre.YAOI, Genre.YaoiArr);
                put(Genre.YURI, Genre.YuriArr);
            }};
        }
    }
}
