# Set up Key Forwarding

Much as we did in [01.7\_SSH\_Key\_Forwarding\_and\_Git](../01.7_SSH_Key_Forwarding_and_Git/README.md) we will want to set up SSH Key Forwarding for our EC2 Instance.

## Enable ForwardAgent
Now we'll need to enable agent forwarding so we can use our GitHub key while ssh'd into our EC2 instance. Once again edit your `~/.ssh/config` and add the following:
```
Host [your_ec2_ip_address]
    ForwardAgent yes
```

By the way, while you're at it, set your ec2 key in here so you don't need to specify it everytime you remote in, as well as the remote User (```ubuntu``). This is the key you created and downloaded in [04.1_Getting_Started_with_EC2](../04.1_Getting_Started_with_EC2/README.md):

```
Host [your_ec2_ip_address]
    ForwardAgent yes
    IdentityFile [path_to_your_ec2_key]
    User ubuntu
```

Now you should be able to just run `ssh [your_ec2_ip_address]` without specifying the key.

## Testing ForwardAgent
ssh into your EC2 instance and run:
```
cloud$ ssh -T git@github.com
```

You should see a success messsage as before.

Next up: go to [Set up MQTT Broker](../04.03_Set_Up_Mqtt_Broker/README.md)

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
