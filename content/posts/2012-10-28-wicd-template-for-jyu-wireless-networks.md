---
title: Wicd template for JYU wireless networks
publishdate: 2012-10-28
tags: [linux]
---

The **jyu-staff** and **jyu-student** wireless networks at the
University of Jyväskylä use EAP-TTLS and PAP for authentication.
Unfortunately, [Wicd](https://launchpad.net/wicd) doesn't ship with a
template for using those networks, so you have to either make one
yourself or search one that works.

<!--more-->

You can find a bunch of templates by
searching for [wicd eduroam templates](http://google.com/?q=wicd+eduroam+template),
but for the sake of convenience here's [one template that works](https://gist.github.com/4693213):

    name = EAP-TTLS-PAP
    author = jkpl
    version = 1
    require identity *Identity password *Password
    optional ca_cert *Path_to_CA_cert anon_identity *Anonymous_Identity
    protected password *Password
    -----
    ctrl_interface=/var/run/wpa_supplicant
    network={
        ssid="$_ESSID"
        scan_ssid=$_SCAN
        key_mgmt=WPA-EAP
        proto=WPA WPA2
        eap=TTLS
        group=CCMP TKIP
        anonymous_identity="$_ANON_IDENTITY"
        ca_cert="$_CA_CERT"
        phase2="auth=PAP"
        identity="$_IDENTITY"
        password="$_PASSWORD"
    }

...and here's how you get it to work:

1.  Download the template file listed above.
2.  Place the downloaded file to your wicd templates directory:
    `/etc/wicd/encryption/templates/eap-ttls-pap`.
3.  Open Wicd network manager.
4.  Depending on whether you are part of the staff or a student, select
    either **jyu-staff** or **jyu-student** network, and open the
    properties for that network.
5.  Make sure you check the option "Use these settings for all networks
    sharing this essid".
6.  Select **EAP-TTLS-PAP** as encryption profile for the network.
7.  Type your JYU credentials (the ones you use in every JYU service) to
    "Indentity" and "Password" fields.
8.  Save the changes, and connect.
