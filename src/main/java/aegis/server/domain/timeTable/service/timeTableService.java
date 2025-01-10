package aegis.server.domain.timeTable.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class timeTableService {

    @Value("${chrome.driver.path}")
    private String chromeDriverPath;

    @Value("${crawl.url}")
    private String crawlUrl;

    //9시 수업의 top px 값
    private static final int NINE_OCLOCK_TOP = 450;

    public List<String> crawlTimetable(){
        List<String> timetableData = new ArrayList<>();

        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        WebDriver driver = new ChromeDriver();

        try{
            // 페이지 열기
            driver.get(crawlUrl);

            //페이지가 로드될 때까지 기다리기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".tablebody")));

            // 강의 정보를 담고 있는 div 요소들 찾기
            List<WebElement> subjects = driver.findElements(By.cssSelector(".subject"));

            // 각 강의 정보 추출하기
            for(WebElement subject : subjects){
                String lectureName = subject.findElement(By.cssSelector("h3")).getText();   // 강의명
                String professorName = subject.findElement(By.cssSelector("em")).getText(); // 교수명
                String classroom = subject.findElement(By.cssSelector("span")).getText();   // 강의실

                if(professorName.isEmpty() || classroom.isEmpty())
                    continue;

                int topPosition = Integer.parseInt(subject.getCssValue("top").replace("px",""));
                int height = Integer.parseInt(subject.getCssValue("height").replace("px",""));

                String classTime = calculateClassTimeFromTopPosition(topPosition, height);


                //강의 정보 리스트에 추가
                timetableData.add("강의명: " + lectureName);
                timetableData.add("교수명: " + professorName);
                timetableData.add("강의실: " + classroom);
                timetableData.add("강의시간: " + classTime);
                timetableData.add("-------------------------");
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            //크롤링 후 브라우저 종료
            driver.quit();
        }

        return timetableData;
    }

    //top 값을 기반으로 강의 시작 시간을 구하고 height값을 통해 강의 종료 시간을 계산
    private String calculateClassTimeFromTopPosition(int topPosition, int height){


        // 강의 시작 시간
        int startHour =9;
        int startMinute = 0;

        //강의 종료 시간
        int endHour =0;
        int endMinute =0;

        // 30분 기준 강의 소요 시간
        int elapsedTime = (height/25)*30;

        // 9시 이후에 시작하는 강의에 대한 px 계산 시작 지점 설정
        if(topPosition > NINE_OCLOCK_TOP)
        {
            int elapsedStartTime = (topPosition - NINE_OCLOCK_TOP)/25 *30;

            startHour += elapsedStartTime/60;
            startMinute += elapsedStartTime%60;

        }

        if(startMinute >=60) {
            startHour += startMinute / 60;
            startMinute = startMinute % 60;
        }

        endHour = startHour + elapsedTime/60;
        endMinute = startMinute + elapsedTime %60;

        // 6시 이후에도 수업이 있는 경우 6시 이전과 px 계산이 다르기 때문에 별도로 강의 종료 시간 추가
        if(topPosition + height > 1000 && (height -1)%75 !=0)
            endMinute += 20;
        else if(topPosition + height > 901 && (height -1)%75 !=0) //6시에 끝나는 경우 끝부분의 px값: 901
            endMinute += 10;

        if(endMinute >=60) {
            endHour += endMinute / 60;
            endMinute = endMinute % 60;
        }



        String startTime = String.format("%02d : %02d", startHour, startMinute);
        String endTime = String.format("%02d : %02d", endHour, endMinute);

        return startTime + " - " + endTime;

    }



}
