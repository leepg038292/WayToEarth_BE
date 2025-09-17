package com.waytoearth.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WayToEarth API Server</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        margin: 0;
                        padding: 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        text-align: center;
                        background: white;
                        padding: 3rem 2rem;
                        border-radius: 15px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                        max-width: 500px;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 1rem;
                        font-size: 2.5rem;
                    }
                    p {
                        color: #666;
                        margin-bottom: 2rem;
                        font-size: 1.1rem;
                    }
                    .links {
                        display: flex;
                        gap: 1rem;
                        justify-content: center;
                        flex-wrap: wrap;
                    }
                    .link {
                        display: inline-block;
                        padding: 0.8rem 1.5rem;
                        background: #667eea;
                        color: white;
                        text-decoration: none;
                        border-radius: 8px;
                        transition: background 0.3s;
                    }
                    .link:hover {
                        background: #5a6fd8;
                    }
                    .status {
                        margin-top: 2rem;
                        padding: 1rem;
                        background: #e8f5e8;
                        border-radius: 8px;
                        color: #2d5a2d;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🌍 WayToEarth</h1>
                    <p>API 서버가 정상적으로 실행 중입니다</p>

                    <div class="links">
                        <a href="/swagger-ui.html" class="link">📚 API 문서</a>
                        <a href="/actuator/health" class="link">💚 헬스체크</a>
                    </div>

                    <div class="status">
                        ✅ Server Status: Online
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}