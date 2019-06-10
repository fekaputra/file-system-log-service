package ac.at.tuwien.logparser.services.util;

import ac.at.tuwien.logparser.entities.*;
import ac.at.tuwien.logparser.entities.Process;
import ac.at.tuwien.logparser.entities.enums.AccessCall;
import ac.at.tuwien.logparser.entities.enums.FileType;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.vocabulary.XSD;
import eu.larkc.csparql.common.RDFTuple;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.log4j.Logger;
import org.apache.xerces.xs.datatypes.XSDateTime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceUtil {

    private final static Logger logger = Logger.getLogger(ServiceUtil.class.getName());

    public static Date parseDate(String timestampLog, String format) throws DateParseException {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            Date date = dateFormat.parse(timestampLog);
            return date;
        } catch (ParseException e) {
            logger.error("Cannot parse date: " + timestampLog);
        }
        logger.error("Date not parsed (problem with format?)" + timestampLog);
        throw new DateParseException("date cannot be parsed: " + timestampLog);
    }

    public static XSDDateTime parseStringToXSDDateTime(String timestamp, String format) throws DateParseException {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = ServiceUtil.parseDate(timestamp, format);
            Date datePlus2Hours = new Date(date.getTime() + (2*60 * 60 * 1000)); //add 2 Hours
            calendar.setTime(datePlus2Hours);
        } catch (DateParseException e) {
            throw new DateParseException("Cannot parse date: " + timestamp + ", " + e);
        }
        return new XSDDateTime(calendar);
        // Creation of XSDDateTime Object used hardcoded GMT timezone --> which leads to a modification of date and 2 hours will be substracted
    }

    public static String parseDateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        String dateString = df.format(date);
        return dateString;
    }

    public static boolean dateWithin10Minutes(Date date) {
        long milliseconds = date.getTime();
        int tenMinutes = 10 * 60 * 1000; //first number is the amount of minutes
        long tenAgo = System.currentTimeMillis() - tenMinutes;
        if (milliseconds < tenAgo) {
            logger.info("searchTimestamp is older than 10 minutes");
            return false;
        } else {
            logger.info("searchTimestamp is within the last 10 minutes");
            return true;
        }
    }

    public static String extractIdFromUrL(String uri) {
        String id = "";
        Pattern regex = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");
        Matcher matcher = regex.matcher(uri);
        while (matcher.find()) {
            String pattern = matcher.group(0);
            id = pattern;
        }
        return id;
    }

    public static LogEntry extractLogEntryFromRDFTruple(RDFTuple t) {
        LogEntry entry = new LogEntry();
        String uri = t.get(0);
        String id = ServiceUtil.extractIdFromUrL(uri);
        entry.setId(id);
        entry.setAccessCall(AccessCall.findByValue(t.get(1)));
        entry.setHasFile(new File(null, t.get(2)));
        entry.setHasProcess(new Process(t.get(3)));
        entry.setHasUser(new User(t.get(4)));
        entry.setOriginatesFrom(new Host(t.get(5)));
        entry.setTimestamp(t.get(6));
        entry.setLogMessage(t.get(7));
        return entry;
    }

    public static String getTimestampFromXSDDate(String xsdDate) {
        return xsdDate.split("\\^")[0];
    }

    public static List<String> extractDirectoryFromFilePaths(String[] paths) {
        List<String> directories = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            java.io.File file = new java.io.File(paths[i]);
            if (file.isFile())
                directories.add(file.getName());
        }
        return directories;
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String removeLeadingDoubleSlash(String path) {
        if (path.startsWith("//")) return path.substring(1);
        else return path;
    }

    public static String generateMD5DigestCheckSumOfFile(String pathname) throws NoSuchFileException {
        try (InputStream is = Files.newInputStream(Paths.get(pathname))) {
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new NoSuchFileException("File does not exist: " + e);
        }
        return null;
    }

    public static void setDateTimeOfEvent(FileAccessEvent event) {
        try {
            Date dateTime = ServiceUtil.parseDate(event.getTimestamp(), "yyyy-MM-dd'T'HH:mm:ss'Z'");
            event.setDateTime(dateTime);
        } catch (DateParseException e) {
            e.printStackTrace();
        }
    }
}
