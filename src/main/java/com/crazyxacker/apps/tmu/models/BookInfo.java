package com.crazyxacker.apps.tmu.models;

import com.crazyxacker.apps.tmu.Main;
import com.crazyxacker.apps.tmu.archive.ArchiveUnpackerFactory;
import com.crazyxacker.apps.tmu.archive.IArchiveUnpacker;
import com.crazyxacker.apps.tmu.managers.LocaleManager;
import com.crazyxacker.apps.tmu.utils.ArrayUtils;
import com.crazyxacker.apps.tmu.utils.FileUtils;
import com.google.gson.stream.JsonReader;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

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
                    "acci??n",
                    "live action",
                    "azione",
                    "a????o",
                    "????????",
                    "????????",
                    "??????????",
                    "????????????",
                    "??????????????"
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
                    "org??a",
                    "??????????????",
                    "18 ????????",
                    "18+",
                    "????????????",
                    "??????????????",
                    "?????? ????????????????",
                    "?????? ????????????????"
            };

            AdventureArr = new String[]{
                    "adventure",
                    "aventure",
                    "aventura",
                    "aventuras",
                    "avventura",
                    "??????????????",
                    "??????????????????????",
                    "??????????????"
            };

            ComedyArr = new String[]{
                    "comedy",
                    "com??die",
                    "com??dia",
                    "comedia",
                    "humor",
                    "commedia",
                    "????????",
                    "??????????????",
                    "??????????????",
                    "??????????????",
                    "??????????????"
            };

            DoujinshiArr = new String[]{
                    "doujinshi",
                    "hentai",
                    "dounshinji",
                    "????????????????"
            };

            DramaArr = new String[]{
                    "drama",
                    "drame",
                    "drammatico",
                    "??????????",
                    "??????????"
            };

            EcchiArr = new String[]{
                    "ecchi",
                    "??????????",
                    "eichii",
                    "????????",
                    "????????",
                    "????????"
            };

            FantasyArr = new String[]{
                    "fantasy",
                    "fantaisie",
                    "fantas??a",
                    "????????",
                    "fantastique",
                    "fantasia",
                    "fantasi",
                    "fantasia negra",
                    "??????????????",
                    "??????????????",
                    "?????????????????????? ??????????????",
                    "??????????????"
            };

            GenderBenderArr = new String[]{
                    "gender bender",
                    "sexedit",
                    "gender intriga",
                    "travelo",
                    "transexual",
                    "?????????????????? ??????????????"
            };

            HaremArr = new String[]{
                    "harem",
                    "har??n",
                    "har??m",
                    "??????????"
            };

            HistoricalArr = new String[]{
                    "historical",
                    "historic",
                    "historique",
                    "hist??rica",
                    "hist??rico",
                    "storico",
                    "????????????",
                    "??????????????",
                    "????????????????????????",
                    "??????????????"
            };

            HorrorArr = new String[]{
                    "horror",
                    "horreur",
                    "??????",
                    "??????????",
                    "????????????",
                    "????????"
            };

            JoseiArr = new String[]{
                    "josei",
                    "dsesay",
                    "??????????",
                    "????????????"
            };

            MagicArr = new String[]{
                    "magic",
                    "magico",
                    "magia",
                    "??????????"
            };

            MartialArtsArr = new String[]{
                    "martial arts",
                    "fightskill",
                    "arts martiaux",
                    "artes marciales",
                    "???????? ????????????",
                    "arte marcial",
                    "artes marciais",
                    "samurai",
                    "???????????? ??????????????????",
                    "?????????????????????? ????????????",
                    "???????????? ??????????????????"
            };

            MechaArr = new String[]{
                    "mecha",
                    "????????"
            };

            MysteryArr = new String[]{
                    "mystery",
                    "myst??re",
                    "misterio",
                    "mist??rio",
                    "misteri",
                    "????????",
                    "??????????????",
                    "??????????????"
            };

            OneShotArr = new String[]{
                    "one shot",
                    "?????? ????????",
                    "????????????"
            };

            PsychologicalArr = new String[]{
                    "psychological",
                    "psycho",
                    "psicologico",
                    "psicol??gico",
                    "????????????",
                    "psychologique",
                    "????????????????????",
                    "??????????????????????????????"
            };

            RomanceArr = new String[]{
                    "romance",
                    "romantica",
                    "romantico",
                    "rom??ntica",
                    "rom??ntico",
                    "rom??ntico",
                    "sentimentale",
                    "??????????????",
                    "??????????????????",
                    "??????????????"
            };

            SchoolLifeArr = new String[]{
                    "school life",
                    "school",
                    "vie scolaire",
                    "escolar",
                    "vida escola",
                    "vida escolar",
                    "scolastico",
                    "???????? ????????????",
                    "??????????"
            };

            SciFiArr = new String[]{
                    "sci fi",
                    "scifi",
                    "fantastic",
                    "science fiction",
                    "ciencia ficci??n",
                    "?????? ??????",
                    "???????? ????????",
                    "science-fiction",
                    "fantascienza",
                    "ci??ncia fic????o",
                    "fic????o cient??fica",
                    "?????????????? ????????????????????",
                    "????????????????????"
            };

            SeinenArr = new String[]{
                    "seinen",
                    "????????????",
                    "????????????",
                    "????????????",
                    "????????????"
            };

            ShoujoArr = new String[]{
                    "shoujo",
                    "shojo",
                    "sh??jo",
                    "????????",
                    "sh??jo",
                    "??????????",
                    "??????????????"
            };

            ShoujoAiArr = new String[]{
                    "shoujo ai",
                    "shoujoai",
                    "?????????? ????",
                    "?????????????? a??"
            };
            ShounenArr = new String[]{
                    "shounen",
                    "shonen",
                    "shonem",
                    "sh??nen",
                    "??????????",
                    "sh??nen",
                    "??????????",
                    "??????????"
            };

            ShounenAiArr = new String[]{
                    "shounen ai",
                    "shounenai",
                    "?????????? ????",
                    "?????????? ????",
                    "???????????? a??"
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
                    "???????? ????????????",
                    "?????????? ???? ????????????",
                    "????????????????????????????",
                    "????????????????????"
            };
            SportsArr = new String[]{
                    "sports",
                    "sport",
                    "deporte",
                    "deportes",
                    "sportivo",
                    "esporte",
                    "esportes",
                    "??????????",
                    "??????????"
            };

            SupernaturalArr = new String[]{
                    "supernatural",
                    "surnaturel",
                    "sobrenatural",
                    "sobrenarutal",
                    "sovrannaturale",
                    "soprannaturale",
                    "?????? ??????????",
                    "????????????????????????????????????",
                    "??????????????????????"
            };

            TragedyArr = new String[]{
                    "tragedy",
                    "trag??die",
                    "tragedia",
                    "trag??dia",
                    "tragico",
                    "??????????",
                    "????????????????"
            };

            YaoiArr = new String[]{
                    "yaoi",
                    "??????"
            };

            YuriArr = new String[]{
                    "yuri",
                    "??????"
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
