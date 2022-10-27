package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.codedeploy.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class AwsCdkCanaryLambdaDemoStack extends Stack {
    public AwsCdkCanaryLambdaDemoStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsCdkCanaryLambdaDemoStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function function = Function.Builder.create(this, "CdkLambdaFunction")
                .code(Code.fromAsset("./lambda-application/target/lambda-application-0.1.jar"))
                .handler("com.myorg.AwsCdkDemoLambdaHandler")
                .functionName("myFunction")
                .runtime(Runtime.JAVA_11)
                .memorySize(256)
                .timeout(Duration.minutes(5))
                .build();

        Version version = function.getCurrentVersion();
        Alias alias = Alias.Builder.create(this, "alias")
                .aliasName("prod")
                .version(version)
                .build();

        LambdaDeploymentConfig config = LambdaDeploymentConfig.Builder
                .create(this, "CdkLambdaCustomConfig")
                .trafficRouting(TrafficRouting.timeBasedCanary(TimeBasedCanaryTrafficRoutingProps.builder()
                        .interval(Duration.minutes(2))
                        .percentage(5)
                        .build()))
                .build();

        LambdaDeploymentGroup deploymentGroup = LambdaDeploymentGroup.Builder
                .create(this, "CdkLambdaCanaryDeployment")
                .alias(alias)
                .deploymentConfig(config)
                .build();
    }
}
