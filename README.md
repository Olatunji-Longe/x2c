# x2c
Excel to CSV converter - (source and destination are currently aws S3 buckets)
This program will try to find AWS access & secret keys in the environment variables of the host machine. Hence, the AWS access Keys (AWS_ACCESS_KEY_ID) & AWS secret key (AWS_SECRET_ACCESS_KEY) need to be configured as environment variables on the machine that will be running this code.

Also deploys as a shaded jar where all dependencies are packaged into a final fat jar. This will enable the artifact to be easily modified and deployed for utilization as an AWS-Lambda.
