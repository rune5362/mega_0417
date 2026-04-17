package com.mega.demo.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Render.com 서버 유지(Keep-Alive)를 위한 컨트롤러
 * Render의 무료 서버는 15분 동안 활동이 없으면 자동으로 꺼집니다. 이 컨트롤러는 주기적으로 자기 자신을 호출하여 서버가 계속 활성 상태를 유지하도록 합니다.
 * - /health-check: 헬스 체크 엔드포인트 (외부나 자기 자신이 호출)
 * - /manual-ping: 브라우저에서 수동으로 호출하여 즉시 자가 호출 테스트
 * - scheduledKeepAlive(): 12분마다 자동으로 자기 자신을 호출하여 서버 유지
 * [주의] MY_APP_URL은 반드시 본인의 Render 서비스 URL + 헬스 체크 엔드포인트로 설정해야 합니다. 예: https://계정아이디.onrender.com/health-check
 */

@RestController
public class RenderKeepAliveController {

    private static final Logger log = LoggerFactory.getLogger(RenderKeepAliveController.class);
    
    // HTTP 요청을 보내기 위한 스프링 도구
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 멀티스레드 환경에서 안전하게 숫자를 계산하기 위한 원자적 정수 변수 (누적 핑 횟수 기록)
    private final AtomicInteger pingCount = new AtomicInteger(0);

    // [설정] 본인의 Render 서비스 URL + 엔드포인트
    private final String MY_APP_URL = "https://mega-0417.onrender.com/";
    // private final String MY_APP_URL = "https://계정아이디.onrender.com/health-check";

    /**
     * 1. 헬스 체크 엔드포인트
     * 외부(UptimeRobot 등)나 자기 자신이 호출하는 경로입니다.
     */
    @GetMapping("/health-check")
    public String healthCheck() {
        // 실제 사람이 접속하거나 서비스가 활성화되면 카운트를 초기화하여 상태 확인
        pingCount.set(0); 
        log.info("🏠 헬스 체크 접속: 카운터가 초기화되었습니다.");
        return "OK - Counter Reset (Current: " + pingCount.get() + ")";
    }

    /**
     * 2. 수동 핑 테스트
     * 브라우저에서 /manual-ping을 입력하여 즉시 자가 호출을 테스트합니다.
     */
    @GetMapping("/manual-ping")
    public String manualPing() {
        sendPing("Manual");
        return "Manual Ping Sent!";
    }

    /**
     * 3. 자동 스케줄러 (핵심 로직)
     * fixedRate: 720,000ms = 12분마다 실행 (Render의 15분 제한보다 짧게 설정)
     */
    @Scheduled(fixedRate = 720000)
    public void scheduledKeepAlive() {
        LocalTime now = LocalTime.now();
        // Render의 무료 서버 리소스를 아끼기 위해 특정 활동 시간대(예: 09:30 ~ 18:00) 설정
        LocalTime start = LocalTime.of(9, 30);
        LocalTime end = LocalTime.of(18, 0);

        // 현재 시간이 설정한 범위 내에 있을 때만 핑을 보냄
        if (now.isAfter(start) && now.isBefore(end)) {
            sendPing("Auto");
        }
    }

    /**
     * 공통 핑 전송 메서드
     * @param type "Manual" 또는 "Auto" 구분
     */
    private void sendPing(String type) {
        try {
            // 설정된 MY_APP_URL로 GET 요청을 보냄 (자기 자신을 호출)
            restTemplate.getForObject(MY_APP_URL, String.class);
            
            // 성공 시 카운트 1 증가
            int currentCount = pingCount.incrementAndGet();
            log.info("🚀 [{}] 핑 전송 완료! (누적: {})", type, currentCount);
        } catch (Exception e) {
            // 서버가 꺼져있거나 네트워크 오류 시 에러 로그 출력
            log.error("❌ [{}] 핑 실패: {}", type, e.getMessage());
        }
    }
}