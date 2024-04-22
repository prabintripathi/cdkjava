package com.myorg;

import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.AmazonLinuxGeneration;
import software.amazon.awscdk.services.ec2.AmazonLinuxImage;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

public class EC2Stack extends Stack {

	public EC2Stack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public EC2Stack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		// Create a Virtual Private Cloud (VPC) with specified configurations:
		Vpc vpc = Vpc.Builder.create(this, "Ec2-Vpc")
				// Set IP addresses range for the VPC
				.ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
				// Set the number of NAT gateways to 0 (0 means no NAT gateways, traffic to the
				// internet will not be routed through NAT)
				.natGateways(0)
				// Configure subnet with specified details
				.subnetConfiguration(List.of(SubnetConfiguration.builder().name("ec2-subnet") // Name of the subnet
						.cidrMask(24) // CIDR mask for subnet (The number of leading bits in the subnet mask)
						.subnetType(SubnetType.PUBLIC) // Type of subnet (PUBLIC, PRIVATE, ISOLATED)
						.build()))
				.build(); // Build the VPC with the specified configurations

		// Create a security group for the web server with specified configurations:
		SecurityGroup webserverSg = SecurityGroup.Builder.create(this, "SecurityGroup").vpc(vpc) // Assign the security
																									// group to the
																									// specified VPC
				.allowAllOutbound(true) // Allow all outbound traffic (default egress rule)
				.build(); // Build the security group

		// Add ingress rules to allow inbound traffic:
		webserverSg.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH publicly"); // Allow SSH traffic
		webserverSg.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP traffic publicly"); // Allow HTTP traffic
		webserverSg.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "Allow HTTPS traffic publicly"); // Allow HTTPS traffic

		// Create an IAM role for the web server with specified configurations:
		Role webserverRole = Role.Builder.create(this, "webserver-role")
				// Specify the service principal that can assume this role (EC2 service in this
				// case)
				.assumedBy(ServicePrincipal.Builder.create("ec2.amazonaws.com").build())
				// Attach managed policies to the role to grant predefined permissions
				.managedPolicies(List.of(ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess"))).build(); // Build the IAM Role

		// Create an EC2 instance for the web server with specified configurations:
		Instance ec2Instance = Instance.Builder.create(this, "ec2-webserver")
				// Assign the instance to the specified VPC
				.vpc(vpc)
				// Specify the subnets in which the instance should be launched (public subnets
				// in this case)
				.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
				// Specify the instance type (e.g., t2.micro)
				.instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
				// Attach the IAM role to the instance
				.role(webserverRole)
				// Attach the security group to the instance
				.securityGroup(webserverSg)
				// Specify the Amazon Machine Image (AMI) for the instance (Amazon Linux 2 in
				// this case)
				.machineImage(
						AmazonLinuxImage.Builder.create().generation(AmazonLinuxGeneration.AMAZON_LINUX_2).build())
				.build(); // Build the EC2 instance

		// Create an output to display the public IP address of the EC2 instance:
		CfnOutput.Builder.create(this, "ec2server").value(ec2Instance.getInstancePublicIp());
	}

}
