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
                <title>WayToEarth API</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
                        background: #fafafa;
                        color: #18181b;
                        line-height: 1.6;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }

                    .container {
                        max-width: 480px;
                        padding: 0 2rem;
                    }

                    .header {
                        text-align: center;
                        margin-bottom: 3rem;
                    }

                    .logo {
                        font-size: 2rem;
                        font-weight: 700;
                        color: #18181b;
                        letter-spacing: -0.02em;
                        margin-bottom: 0.5rem;
                    }

                    .subtitle {
                        color: #71717a;
                        font-size: 0.95rem;
                        font-weight: 400;
                    }

                    .status {
                        display: inline-flex;
                        align-items: center;
                        gap: 0.5rem;
                        background: #f4f4f5;
                        padding: 0.75rem 1rem;
                        border-radius: 6px;
                        margin-bottom: 2rem;
                        font-size: 0.875rem;
                        color: #52525b;
                        border: 1px solid #e4e4e7;
                    }

                    .status-dot {
                        width: 8px;
                        height: 8px;
                        background: #22c55e;
                        border-radius: 50%;
                    }

                    .nav {
                        display: grid;
                        gap: 0.75rem;
                    }

                    .nav-item {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 1rem;
                        background: white;
                        border: 1px solid #e4e4e7;
                        border-radius: 8px;
                        text-decoration: none;
                        color: #18181b;
                        transition: all 0.15s ease;
                    }

                    .nav-item:hover {
                        border-color: #d4d4d8;
                        background: #f9f9f9;
                    }

                    .nav-item-left {
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                    }

                    .nav-item-icon {
                        width: 20px;
                        height: 20px;
                        color: #71717a;
                    }

                    .nav-item-title {
                        font-weight: 500;
                        font-size: 0.95rem;
                    }

                    .nav-item-arrow {
                        color: #a1a1aa;
                        font-size: 0.75rem;
                    }

                    .footer {
                        margin-top: 2.5rem;
                        text-align: center;
                        color: #a1a1aa;
                        font-size: 0.8rem;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">WayToEarth</div>
                        <div class="subtitle">REST API Server</div>
                    </div>

                    <div class="status">
                        <div class="status-dot"></div>
                        <span>Service Online</span>
                    </div>

                    <nav class="nav">
                        <a href="/swagger-ui.html" class="nav-item">
                            <div class="nav-item-left">
                                <svg class="nav-item-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                                </svg>
                                <span class="nav-item-title">API Documentation</span>
                            </div>
                            <span class="nav-item-arrow">→</span>
                        </a>

                        <a href="/actuator/health" class="nav-item">
                            <div class="nav-item-left">
                                <svg class="nav-item-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                                </svg>
                                <span class="nav-item-title">Health Check</span>
                            </div>
                            <span class="nav-item-arrow">→</span>
                        </a>
                    </nav>

                    <div class="footer">
                        API Base URL: /v1/
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}