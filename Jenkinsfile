// 자주 사용되는 필요한 변수를 전역으로 선언하는 것도 가능.
def ecrLoginHelper = "docker-credential-ecr-login" // ECR credential helper 이름

// 젠킨스의 선언형 파이프라인 정의부 시작 (그루비 언어)
pipeline {
    agent any // 어느 젠킨스 서버에서나 실행이 가능
    environment {
        SERVICE_DIRS = "config-service,discovery-service,gateway-service,user-service,ordering-service,product-service"
        ECR_URL = "390844784325.dkr.ecr.ap-northeast-2.amazonaws.com"
        REGION = "ap-northeast-2"
        K8S_REPO_CRED = "K8S_REPO_CRED"
    }
    stages {
        // 각 작업 단위를 스테이지로 나누어서 작성 가능.
        stage('Pull Codes from Github') { // 스테이지 제목 (맘대로 써도 됨)
            steps {
                checkout scm // 젠킨스와 연결된 소스 컨트롤 매니저(git 등)에서 코드를 가져오는 명령어
            }
        }

        stage('Add Secret To config-service') {
            steps {
                withCredentials([file(credentialsId: 'config-secret', variable: 'configSecret')]) {
                    script {
                        sh 'cp $configSecret config-service/src/main/resources/application-dev.yml'
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // rev-list: 특정 브랜치나 커밋을 기준으로 모든 이전 커밋 목록을 나열
                    // --count: 목록 출력 말고 커밋 개수만 숫자로 반환
                    def commitCount = sh(script: "git rev-list --count HEAD", returnStdout: true)
                                        .trim()
                                        .toInteger()
                    def changedServices = []
                    def serviceDirs = env.SERVICE_DIRS.split(",")

                    if (commitCount == 1) {
                        // 최초 커밋이라면 모든 서비스 빌드
                        echo "Initial commit detected. All services will be built."
                        changedServices = serviceDirs // 변경된 서비스는 모든 서비스다.

                    } else {
                        // 변경된 파일 감지
                        def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true)
                                            .trim()
                                            .split('\n') // 변경된 파일을 줄 단위로 분리

                        // 변경된 파일 출력
                        // [user-service/src/main/resources/application.yml,
                        // user-service/src/main/java/com/playdata/userservice/controller/UserController.java,
                        // ordering-service/src/main/resources/application.yml]
                        echo "Changed files: ${changedFiles}"

                        serviceDirs.each { service ->
                            // changedFiles라는 리스트를 조회해서 service 변수에 들어온 서비스 이름과
                            // 하나라도 일치하는 이름이 있다면 true, 하나도 존재하지 않으면 false
                            // service: user-service -> 변경된 파일 경로가 user-service/로 시작한다면 true
                            if (changedFiles.any { it.startsWith(service + "/") }) {
                                changedServices.add(service)
                            }
                        }
                    }

                    //변경된 서비스 이름을 모아놓은 리스트를 다른 스테이지에서도 사용하기 위해 환경 변수로 선언.
                    // join() -> 지정한 문자열을 구분자로 하여 리스트 요소를 하나의 문자열로 리턴. 중복 제거.
                    // 환경변수는 문자열만 선언할 수 있어서 join을 사용함.
                    env.CHANGED_SERVICES = changedServices.join(",")
                    if (env.CHANGED_SERVICES == "") {
                        echo "No changes detected in service directories. Skipping build and deployment."
                        // 성공 상태로 파이프라인을 종료
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }

        stage('Build Changed Services') {
            // 이 스테이지는 빌드되어야 할 서비스가 존재한다면 실행되는 스테이지.
            // 이전 스테이지에서 세팅한 CHANGED_SERVICES라는 환경변수가 비어있지 않아야만 실행.
            when {
                expression { env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                   def changedServices = env.CHANGED_SERVICES.split(",")
                   changedServices.each { service ->
                        sh """
                        echo "Building ${service}..."
                        cd ${service}
                        ./gradlew clean build -x test
                        ls -al ./build/libs
                        cd ..
                        """
                   }
                }
            }
        }

        stage('Build Docker Image & Push to AWS ECR') {
            when {
                expression { env.CHANGED_SERVICES != "" }
            }
            steps {
                script {
                    // jenkins에 저장된 credentials를 사용하여 AWS 자격증명을 설정.
                    withAWS(region: "${REGION}", credentials: "aws-key") {
                        def changedServices = env.CHANGED_SERVICES.split(",")
                        changedServices.each { service ->
                            // 여기서 원하는 버전을 정하거나, 커밋 태그 등을 붙여서 이미지 이름을 만들자. (빌드 번호)
                            def newTag = "1.0.3"

                            sh """
                            # ECR에 이미지를 push하기 위해 인증 정보를 대신 검증해 주는 도구 다운로드.
                            # /usr/local/bin/ 경로에 해당 파일을 이동
                            curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
                            chmod +x ${ecrLoginHelper}
                            mv ${ecrLoginHelper} /usr/local/bin/

                            # Docker에게 push 명령을 내리면 지정된 URL로 push할 수 있게 설정.
                            # 자동으로 로그인 도구를 쓰게 설정
                            mkdir -p ~/.docker
                            echo '{"credHelpers": {"${ECR_URL}": "ecr-login"}}' > ~/.docker/config.json

                            docker build -t ${service}:${newTag} ${service}
                            docker tag ${service}:${newTag} ${ECR_URL}/${service}:${newTag}
                            docker push ${ECR_URL}/${service}:${newTag}
                            """
                        }
                    }
                }
            }
        }

        stage('Update k8s Repo') {
            when {
                expression { env.CHANGED_SERVICES != "" } // 변경된 서비스가 있을 때에만 실행
            }

            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "${K8S_REPO_CRED}", usernameVariable: "GIT_USERNAME", passwordVariable: 'GIT_PASSWORD')]) {
                        // k8s 레포지토리를 클론하자.
                        // 현재 stage가 활동하는 경로는 /var/jenkins_home/workspace/pipeline폴더
                        // pipeline폴더 말고 workspace에 클론 받고 싶어서 cd .. 실행
                        sh '''
                            cd ..
                            ls -a
                            git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/uBing9201/orderservice-k8s.git
                        '''

                        def changedServices = env.CHANGED_SERVICES.split(",")
                        changedServices.each { service ->
                            def newTag = "1.0.3" // 이미지 빌드할 때 사용한 태그를 동일하게 사용 (환경변수에 넣어넣고 끌고와도 됨.)

                            // msa-chart/charts/<service>/values.yaml 파일 내의 image 태그를 교체
                            // sed: 스트림 편집기(stream editor), 텍스트 파일을 수정하는 데 사용.
                            // s#^ -> 라인의 시작을 의미.
                            // image: 텍스트 image: 이라는 것을 찾아라.
                            // .*image: image 다음에 오는 모든 문자
                            // 종합: 'image:' <- 요렇게 시작하는 텍스트를 찾아서 image: 다음에 오는 문자를 내가 지정한 텍스트로 수정.
                            sh """
                                cd /var/jenkins_home/workspace/orderservice-k8s2
                                ls -a
                                echo "Updating ${service} image tag in k8s repo..."
                                sed -i 's#^image: .*#image: ${ECR_URL}/${service}:${newTag}#' ./msa-chart/charts/${service}/values.yaml
                            """
                        }

                        // values.yaml 파일의 image 태그가 수정이 완료되면
                        // ArgoCD가 담당하는 깃 저장소로 변경사항을 commit & push
                        // 마지막에 클론한 프로젝트 폴더를 지우는 이유는, 다음 파이프라인 로직을 위해서.
                        // 기존에 폴더가 존재한다면 다음 clone 시에 에러가 발생하면서 파이프라인이 멈춰요.
                        sh """
                            cd /var/jenkins_home/workspace/orderservice-k8s
                            git config user.name "uBing9201"
                            git config user.email "ubing0101@kakao.com"
                            git remote -v
                            git add .
                            git commit -m "Update images for changed services ${env.BUILD_ID}"
                            git push origin main

                            echo "push complete"
                            cd ..
                            rm -rf orderservice-k8s
                            ls -a
                        """
                    }
                }
            }
        }
    }
}