package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketAccessControl;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.constructs.Construct;

public class CdkJavaIacProjectStack extends Stack {
    public CdkJavaIacProjectStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkJavaIacProjectStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = Bucket.Builder.create(this, "my-example-bucket")
                .encryption(BucketEncryption.KMS_MANAGED)
                .accessControl(BucketAccessControl.BUCKET_OWNER_FULL_CONTROL)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .versioned(true)
                .build();

        // add  tag to bucket
        Tags.of(bucket).add("prabin-backup", "true");

    }
}
