pipeline {
	options {
		timeout(time: 40, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: (env.BRANCH_NAME == 'master' || env.BRANCH_NAME ==~ 'BETA.*') ? '100':'5', artifactNumToKeepStr: (env.BRANCH_NAME == 'master' || env.BRANCH_NAME ==~ 'BETA.*') ? '15':'2'))
		disableConcurrentBuilds(abortPrevious: true)
		timestamps()
	}
	agent {
		label "ubuntu-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk25-latest'
	}
	environment {
		NON_MODULAR_JAVA_HOME = tool(type:'jdk', name:'temurin-jdk8-latest')
	}
	stages {
		stage('Build') {
			steps {
				xvnc(useXauthority: true) {
					sh """
					mvn clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
						-Ptest-on-javase-25 -Pbree-libs -Papi-check -Pjavadoc\
						-Dmaven.test.failure.ignore=true\
						-Dcompare-version-with-baselines.skip=false \
						-Dproject.build.sourceEncoding=UTF-8 \
						-DDetectVMInstallationsJob.disabled=true \
						-Dtycho.apitools.debug \
						-DtrimStackTrace=false
					"""
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					// The following lines use the newest build on master that did not fail a reference
					// To not fail master build on failed test maven needs to be started with "-Dmaven.test.failure.ignore=true" it will then only marked unstable.
					// To not fail the build also "unstable: true" is used to only mark the build unstable instead of failing when qualityGates are missed
					// To accept unstable builds (test errors or new warnings introduced by third party changes) as reference using "ignoreQualityGate:true"
					// To only show warnings related to the PR on a PR using "publishAllIssues:false"
					discoverGitReferenceBuild referenceJob: 'eclipse.jdt.debug-github/master'
					junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
					recordIssues publishAllIssues: false, ignoreQualityGate: true, enabledForFailure: true, tools: [
							eclipse(name: 'Compiler', pattern: '**/target/compilelogs/*.xml'),
							issues(name: 'API Tools', id: 'apitools', pattern: '**/target/apianalysis/*.xml'),
							javaDoc(), 
						], qualityGates: [[threshold: 1, type: 'DELTA', unstable: true]]
					recordIssues tools: [mavenConsole()]
				}
			}
		}
	}
}
