package aegis.server.domain.timetable.dto.external;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.ArrayList;

@Data
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class EverytimeResponse {

    @XmlElement(name = "table")
    private Table table;

    @lombok.Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Table {
        @XmlAttribute
        private String year;
        @XmlAttribute
        private String semester;
        @XmlElement(name = "subject")
        private ArrayList<Subject> subjects;
    }

    @lombok.Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Property {
        @XmlAttribute
        private String value;
    }

    @lombok.Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Subject {
        @XmlElement
        private Property name;
        @XmlElement
        private Property professor;
        @XmlElement
        private Property credit;
        @XmlElement
        private Time time;
    }

    @lombok.Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Time {
        @XmlElement
        private ArrayList<Data> data;
    }

    @lombok.Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Data {
        @XmlAttribute
        private Integer day;
        @XmlAttribute(name = "starttime")
        private Long startTime;
        @XmlAttribute(name = "endtime")
        private Long endTime;
        @XmlAttribute
        private String place;
    }
}

//<?xml version="1.0" encoding="UTF-8"?>
//<response>
//  <table year="2024" semester="2" status="1" identifier="Redte0RhSbJRibpczui0">
//    <subject id="7002187">
//      <internal value="540650-1"/>
//      <name value="데이터사이언스"/>
//      <professor value="오세종"/>
//      <time value="수15~19(미디어102)">
//        <data day="2" starttime="192" endtime="226" place="미디어102"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002195">
//      <internal value="472570-1"/>
//      <name value="디지털논리회로실험"/>
//      <professor value="최천원"/>
//      <time value="화10~13(2공421)">
//        <data day="1" starttime="162" endtime="186" place="2공421"/>
//      </time>
//      <place value=""/>
//      <credit value="1"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002200">
//      <internal value="514740-3"/>
//      <name value="OS/NW실습"/>
//      <professor value="김승훈"/>
//      <time value="목13~18(2공521)">
//        <data day="3" starttime="180" endtime="216" place="2공521"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002208">
//      <internal value="527880-1"/>
//      <name value="소프트웨어공학(CE)"/>
//      <professor value="남재현"/>
//      <time value="월4~6(2공105)&lt;br>목1~3(2공105)">
//        <data day="0" starttime="126" endtime="144" place="2공105"/>
//        <data day="3" starttime="108" endtime="126" place="2공105"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002213">
//      <internal value="524920-1"/>
//      <name value="알고리즘및실습"/>
//      <professor value="김준모"/>
//      <time value="화1~3(2공524)&lt;br>수10~12(2공524)">
//        <data day="1" starttime="108" endtime="126" place="2공524"/>
//        <data day="2" starttime="162" endtime="180" place="2공524"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002214">
//      <internal value="514770-1"/>
//      <name value="자바프로그래밍2"/>
//      <professor value="박경신"/>
//      <time value="수1~6(2공521)">
//        <data day="2" starttime="108" endtime="144" place="2공521"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//    <subject id="7002219">
//      <internal value="426470-1"/>
//      <name value="프로그래밍언어"/>
//      <professor value="박태근"/>
//      <time value="월9~11(소프트310)&lt;br>목4~6(소프트310)">
//        <data day="0" starttime="156" endtime="174" place="소프트310"/>
//        <data day="3" starttime="126" endtime="144" place="소프트310"/>
//      </time>
//      <place value=""/>
//      <credit value="3"/>
//      <closed value="0"/>
//    </subject>
//  </table>
//</response>