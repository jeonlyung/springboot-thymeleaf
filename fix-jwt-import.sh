#!/bin/bash

echo "🔧 JWT 라이브러리 인식 오류 자동 수정 스크립트"
echo "=============================================="
echo ""

cd /Users/hsj/intellij-workspace/springboot-thymeleaf

echo "1️⃣ Gradle 캐시 정리..."
./gradlew clean --refresh-dependencies

echo ""
echo "2️⃣ JWT 라이브러리 다운로드 확인..."
./gradlew dependencies --configuration compileClasspath | grep jjwt

echo ""
echo "3️⃣ 컴파일 테스트..."
./gradlew compileJava

echo ""
echo "✅ 완료! 이제 IntelliJ에서 다음을 실행하세요:"
echo "   1. View → Tool Windows → Gradle"
echo "   2. 새로고침 아이콘(🔄) 클릭"
echo "   3. 또는 Cmd+Shift+I (Import Changes)"
echo ""
echo "💡 여전히 에러가 보이면:"
echo "   File → Invalidate Caches → Invalidate and Restart"

