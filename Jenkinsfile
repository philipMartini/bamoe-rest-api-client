pipeline
{
	agent 
	{
        label 'sky-mdwgateway-dev'
    }
    environment
    {
    	DOCKER_BUILD_NAME = 'ita-integration-hermes-camunda-client'
        DOCKER_RUN_PARAMS = "-v ${WORKSPACE}:/usr/src/project -u jenkins -w /usr/src/project"
    	MAVEN_REPO_SNAPSHOT_ID="snapshots-repo"
    	MAVEN_REPO_SNAPSHOT_NAME="maven-snapshots"
    	MAVEN_REPO_SNAPSHOT_URL="http://nexus.cicd.prod.sky.aws:8080/repository/maven-snapshots/"
    	MAVEN_REPO_RELEASE_ID="releases-repo"
    	MAVEN_REPO_RELEASE_NAME="maven-releases"
    	MAVEN_REPO_RELEASE_URL="http://nexus.cicd.prod.sky.aws:8080/repository/maven-releases/"
    	MAVEN_REPO_PUBLIC_ID="nexus"
    	MAVEN_REPO_PUBLIC_NAME="maven-public"
    	MAVEN_REPO_PUBLIC_URL="http://nexus.cicd.prod.sky.aws:8080/repository/maven-public/"
    	MAVEN_OPTS="-Dhttps.protocols=TLSv1.2 -Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository -Dorg.slf4j.simpleLogger.showDateTime=true -Djava.awt.headless=true"
    	MAVEN_CLI_OPTS="--batch-mode --errors --fail-at-end --show-version -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=WARN -DinstallAtEnd=false -DdeployAtEnd=false -s settings.xml"
	}
    stages 
    {
    	stage ('Init')
    	{
    		steps
    		{
	    		withCredentials([usernamePassword(credentialsId: "jenkins_user_nexus", usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) 
	    		{
	            	sh(script: 'sed -i -e s/MAVEN_REPO_USER/${NEXUS_USERNAME}/g ./settings.xml')
	                sh(script: 'sed -i -e s/MAVEN_REPO_PASS/${NEXUS_PASSWORD}/g ./settings.xml')
				}
				sh("cp settings.xml ./script/build/")
				sh("docker build -t ${DOCKER_BUILD_NAME} ./script/build")
			}
		}
    	stage ('Clean')
    	{
    		steps
    		{
    			script
    			{
    				docker.image("${DOCKER_BUILD_NAME}").inside(DOCKER_RUN_PARAMS) 
    				{
        				sh 'mvn $MAVEN_OPTS $MAVEN_CLI_OPTS clean'
        			}
        		}        
			}    
		}
        stage ('Build') 
        {
        	environment
        	{
        	    SCANNER_HOME = tool 'sonar Scanner'
    			SONAR_HOST_URL = "http://sonar.cicd.prod.sky.aws:8080"
			}
            steps 
            {
            	script
    			{
    				mavenProfile = ""
                	if (params.sourceAndJavadoc)
                	{
                	    mavenProfile = "-Ppackaging"
					}
    				docker.image("${DOCKER_BUILD_NAME}").inside(DOCKER_RUN_PARAMS) 
    				{
    					withSonarQubeEnv('Sonar')
    					{
                			sh 'mvn $MAVEN_OPTS $MAVEN_CLI_OPTS package sonar:sonar ' + mavenProfile
                		}
                	}
                }
            }
            post
            {
            	success 
            	{
                    junit 'target/surefire-reports/**/*.xml'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        stage ('Quality Gate') 
        {        	
            steps 
            {
            	timeout(time: 1, unit: 'HOURS') 
            	{
                	waitForQualityGate abortPipeline: false
               	}
            }
        }
        stage ('Upload Nexus') 
        {
        	when
        	{
        		anyOf
        		{
        			branch 'develop';
        			branch 'release*';
        			branch 'master';
        			branch 'main';
        			buildingTag()    
				}
        	}
        	steps 
            {            	
            	script
            	{
            		docker.image(DOCKER_BUILD_NAME).inside(DOCKER_RUN_PARAMS) 
            		{
	            		REPOSITORY = ""
	            		GROUP_ID = sh(returnStdout: true, script: 'mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -s settings.xml -Dexpression=project.groupId | grep -Ev "(^[0-9 ]*\\[|Download\\w+)"').trim()
	            		ARTIFACT_ID = sh(returnStdout: true, script: 'mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -s settings.xml -Dexpression=project.artifactId | grep -Ev "(^[0-9 ]*\\[|Download\\w+)"').trim()
	   	            	PROJECT_VERSION = sh(returnStdout: true, script: 'mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -s settings.xml -Dexpression=project.version | grep -Ev "(^[0-9 ]*\\[|Download\\w+)"').trim()
	   	            	if (PROJECT_VERSION.endsWith("SNAPSHOT"))
	   	            	{
	   	            		REPOSITORY = MAVEN_REPO_SNAPSHOT_NAME
	    				}
	    				else
	    				{
	    					REPOSITORY = MAVEN_REPO_RELEASE_NAME
	    				}
	    				
	    				baseFileName = ARTIFACT_ID + '-' + PROJECT_VERSION
	    				includeSources = fileExists('target/' + baseFileName + '-sources.jar')
	    				includeJavadoc = fileExists('target/' + baseFileName + '-javadoc.jar')
		            	echo "About to deploy $GROUP_ID:$ARTIFACT_ID:$PROJECT_VERSION to $REPOSITORY (include source = $includeSources, include javadoc = $includeJavadoc)"
		            	uploadToNexus(REPOSITORY, GROUP_ID, ARTIFACT_ID, PROJECT_VERSION, 'target/' + baseFileName + '.jar', 'jar', '')
		            	uploadToNexus(REPOSITORY, GROUP_ID, ARTIFACT_ID, PROJECT_VERSION, 'pom.xml', 'pom', '')
		            	
	    				if (includeSources)
	    				{
		    				uploadToNexus(REPOSITORY, GROUP_ID, ARTIFACT_ID, PROJECT_VERSION, 'target/' + baseFileName + '-sources.jar', 'jar', 'sources')
						}
						
						if (includeJavadoc)
	    				{
	    					uploadToNexus(REPOSITORY, GROUP_ID, ARTIFACT_ID, PROJECT_VERSION, 'target/' + baseFileName + '-javadoc.jar', 'jar', 'javadoc')
						}
					}
            	}
            }
        }
    }
}

void uploadToNexus(String aRepository, String aGroupId, String aArtifactId, String aVersion, String aFile, String aType, String aClassifier)
{
    nexusArtifactUploader nexusVersion: 'nexus3',
    	protocol: 'http',
    	nexusUrl: 'nexus.cicd.prod.sky.aws:8080',
    	credentialsId: 'jenkins_user_nexus',
    	repository: aRepository, 
    	groupId: aGroupId,
    	artifacts: 
    	[
			[
				artifactId: aArtifactId, 
			    classifier: aClassifier, 
			    file: aFile, 
		   	    type: aType
	  		]
	    ], 
		version: aVersion
}