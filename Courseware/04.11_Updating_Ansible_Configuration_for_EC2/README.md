# Updating Ansible Configuration for EC2

Now that our system includes not just a Raspberry Pi but also an AWS EC2 instance, our Ansible plabooks must also include EC2.

The `ansible/hosts.template` file has been updated to include an ```ec2``` host and a new 'cloud_broker' group:

```
# Ansible Hosts file
# this is the list of hosts (computers) and groups of hosts to be managed
#  by ansible


# replace <IP> with the IP address of your LAMPi
#   (e.g., "ansible_ssh_host=10.0.1.34")
#
# from Chaper 2 Solution forward, we need to use Public/Private keys
#   instead of passwords for Ansible, for key-forwarding and general
#   security
lampi ansible_user=pi ansible_ssh_host=<IP>

# replace <IP> with the IP address (or DNS) of your EC2 instance
#  (e.g., "ansible_ssh_host=10.0.1.34")
ec2  ansible_user=ubuntu ansible_ssh_host=<IP>

# our "group" of LAMPIs, listing the hostname for each on a separate line
[lampis]
lampi

# our cloud broker - only expect one of these
[cloud_brokers]
ec2
```

Be sure to update your `hosts` file to use the updated `ansible/hosts.template` and add your Elastic IP (EIP) hostname or IP address.

Also, be sure to update your SSH configuration to allow SSH Key Forwarding to your EC2 instance.

&copy; 2015-2020 LeanDog, Inc. and Nick Barendt
