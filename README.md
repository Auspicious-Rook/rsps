# Building the server
## Debian LXC in Proxmox

### Walkthrough
1. Start your Linux LXC/Host/VM.
2. Save your IP for later: `ip a`
3. Update and add the following tools: 
	1. `apt update`
	2. `apt install git curl unzip zip`
4. Install the Java SKD: `curl -s "https://get.sdkman.io" | bash`
5. Start a new shell or run `chmod +x .sdkman/bin/sdkman-init.sh`
6. Install the correct JDK: `sdk install java 21.0.7-tem`
7. Install the correct Gradle: `sdk install gradle 8.11`
8. Clone the Alter repo. Command: `git clone https://github.com/AlterRSPS/Alter.git`
9. Change to the Alter directory: `cd Alter`
10. We need to install cache files as well as xteas. change directory to the data location: `cd data`
11. Set the Gradle version: `./gradlew wrapper --gradle-version 8.11 --distribution-type bin`
12. I think you need to run this one as well. (not for sure): `gradle wrapper --gradle-version 8.11`
13. Get and change the name of the JSON file:
	1. `wget -o xteas.json https://archive.openrs2.org/caches/runescape/2038/keys.json`
	2. `mv keys.json xteas.json`
14. Make and change to a test directory: `mkdir test && cd test`
15. Pull down the updated cache files: `wget https://archive.openrs2.org/caches/runescape/2038/disk.zip`
16. Unzip and move them to the correct location: 	  ```
    ```
       rm /root/Alter/data/cache
	   unzip disk.zip
	   mv /root/Alter/data/test/cache/ /root/Alter/data/
	```
17. change back to the Alter directory: `cd .. cd ..`
18. Install the server: `./gradlew :game-server:install`
19. Grab the Modulus code. It will be printed once the server is done installing. You HAVE to HAVE THIS or you cannot connect to your new server
20. Run the server: `./gradlew :game-server:run`
21. Log in with any credentials you want. The initial login creates the player and the password file. Do not forget them or you have to make a new player or remove the player file.
22. edit the player file: `nano data/saves/details/{{ACCOUNT_NAME_HERE}}`
	1. Change the line that says privilege to `ADMINSTRATOR`
23. You are now an admin, enjoy!
#### The full command stack to get your server running.
```
ip a
apt update
apt install git
apt install curl
apt install unzip
apt install zip
git clone https://github.com/AlterRSPS/Alter.git
curl -s "https://get.sdkman.io" | bash
chmod +x .sdkman/bin/sdkman-init.sh
chmod +x /root/.sdkman/bin/sdkman-init.sh
/root/.sdkman/bin/sdkman-init.sh
sdk install java 21.0.7-tem
cd Alter/
sdk install gradle 8.11
./gradlew wrapper --gradle-version 8.11 --distribution-type bin
gradle wrapper --gradle-version 8.11
./gradlew :game-server:install
cd data
wget -o xteas.json https://archive.openrs2.org/caches/runescape/2038/keys.json
mv keys.json xteas.json
mkdir test && cd test
wget https://archive.openrs2.org/caches/runescape/2038/disk.zip
unzip disk.zip
cd ..
cd data
rm /root/Alter/data/cache

mv /root/Alter/data/test/cache/ /root/Alter/data/
cd ..
cd ..
./gradlew :game-server:install
./gradlew :game-server:run

nano data/saves/details/{{ACCOUNT_NAME_HERE}}
```

## Building the client for external hosting

### Creating the world File

Things get fun here. In order to host externally, you need a file to point the client to the right IP. This has to be made manually as far as I am aware so buckle up for an adventure.

#### Setup
I use a Windows VM for this next part. You can use your host too. I have yet to do it in Linux.

1. Download [Intellij]([Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows)) or your favorite Java SDK.
2. Load up Intellij , click at the project in the top, and hit clone a new repo.
3. Clone [rsprox](https://github.com/blurite/rsprox.git)
4. Load the Gradle scripts when it prompts you to

#### Building the world 
On the left file area, browse/expand to this section: `C:\Users\User\IdeaProjects\rsprox\proxy\src\main\kotlin\net\rsprox\proxy\worlds\`
1. Right click and create a new file named `builder.kt`
2. Paste the following code. You will need to update the IPs to make it yours:
```kotlin
package tools

import io.netty.buffer.ByteBufAllocator
import java.nio.file.Files
import java.nio.file.Paths
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldList

public fun main() {
    // Define proxy target configuration
    val config: ProxyTargetConfig = ProxyTargetConfig(
        id = 0,
        name = "LocalProxy",
        javConfigUrl = "http://oldschool.runescape.com/jav_config.ws",
        modulus = null,
        varpCount = 0,
        revision = "230",
        runeliteBootstrapCommitHash = null,
        runeliteGamepackUrl = null
    )

    // Define first world
    val localWorld: World = World(
        proxyTargetConfig = config,
        id = 255,
        properties = 0, // Set flags like members, PvP, etc. here
        population = 1337,
        location = 0,
        host = "10.0.0.24", // Your LAN IP or server host
        activity = "Local Dev World"
    )

    // Define second world (optional)
    val exampleWorld: World = World(
        proxyTargetConfig = config,
        id = 301,
        properties = 0,
        population = 999,
        location = 0,
        host = "10.0.0.24",
        activity = "Example World"
    )

    // Create world list and encode it
    val worldList: WorldList = WorldList(listOf(localWorld, exampleWorld))
    val encoded = worldList.encode(ByteBufAllocator.DEFAULT)

    // Read encoded buffer into byte array
    val bytes = ByteArray(encoded.readableBytes())
    encoded.buffer.readBytes(bytes)

    // Write to file
    Files.write(Paths.get("world_list.ws"), bytes)

    println("✅ Successfully wrote world_list.ws with ${worldList.size} world(s).")
}  
	   
 ```
3.  This script will output a `world_list.ws` file in the working directory: `C:\Users\User\IdeaProjects\rsprox\world_list.ws`
4. Save this world list and host is publicly. I used github for mine.

#### Function Check

1. On the bottom left, activate the terminal in Intellij
2. Enter the following command: ` ./gradlew build`
3. Start the proxy with: ` ./gradlew proxy`
#### Java Config File
1. Create a new file named `jav.ws` name doesn't matter just end it in `.ws`
2. Paste the following contents into the file. Note you change your code parameter to what your server IP is and change param 17 to where your world list is hosted. You may need to change param 25 if you are not using revision 228 of OSRS
3. Host this file publicly like the last one.
 ```yaml
title=Blurite Alpha
adverturl=http://www.runescape.com/g=oldscape/bare_advert.ws
codebase=http://10.0.0.24/
cachedir=blurite
storebase=0
initial_jar=gamepack_2221869.jar
initial_class=client.class
termsurl=http://www.jagex.com/g=oldscape/terms/terms.ws
privacyurl=http://www.jagex.com/g=oldscape/privacy/privacy.ws
viewerversion=124
win_sub_version=1
mac_sub_version=2
other_sub_version=2
browsercontrol_win_x86_jar=browsercontrol_0_-1928975093.jar
browsercontrol_win_amd64_jar=browsercontrol_1_1674545273.jar
gedigesturl=https://secure.runescape.com/m=itemdb_oldschool/g=oldscape/digest.csv
download=2503642
window_preferredwidth=800
window_preferredheight=600
advert_height=96
applet_minwidth=765
applet_minheight=503
applet_maxwidth=5760
applet_maxheight=2160
msg=lang0=English
msg=tandc=This game is copyright © 1999 - 2025 Jagex Ltd.\Use of this game is subject to our ["https://legal.jagex.com/docs/terms"Terms and Conditions] and ["https://legal.jagex.com/docs/policies/privacy"Privacy Policy]. ["https://legal.jagex.com/docs/policies/privacy/exercising-your-rights"Do Not Sell Or Share My Personal Information].
msg=options=Options
msg=language=Language
msg=changes_on_restart=Your changes will take effect when you next start this program.
msg=loading_app_resources=Loading application resources
msg=err_verify_bc64=Unable to verify browsercontrol64
msg=err_verify_bc=Unable to verify browsercontrol
msg=err_load_bc=Unable to load browsercontrol
msg=loading_app=Loading application
msg=err_create_target=Unable to create target applet
msg=err_create_advertising=Unable to create advertising
msg=err_save_file=Error saving file
msg=err_downloading=Error downloading
msg=ok=OK
msg=cancel=Cancel
msg=message=Message
msg=copy_paste_url=Please copy and paste the following URL into your web browser
msg=information=Information
msg=err_get_file=Error getting file
msg=new_version=Update available! You can now launch the client directly from the OldSchool website.\nGet the new version from the link on the OldSchool homepage: http://oldschool.runescape.com/
msg=new_version_linktext=Open OldSchool Homepage
msg=new_version_link=http://oldschool.runescape.com/
param=21=0
param=4=1
param=7=0
param=13=.runescape.com
param=18=
param=25=228
param=5=1
param=6=0
param=2=https://payments.jagex.com/
param=17=https://github.com/Auspicious-Rook/rsps/raw/refs/heads/main/world_list.ws
param=20=https://social.auth.jagex.com/
param=11=https://auth.jagex.com/
param=19=196515767263-1oo20deqm6edn7ujlihl6rpadk9drhva.apps.googleusercontent.com
param=8=true
param=28=https://account.jagex.com/
param=14=0
param=3=true
param=9=ElZAIrq5NpKN6D3mDdihco3oPeYN2KFy2DCquj7JMmECPmLrDP3Bnw
param=16=false
param=22=https://auth.runescape.com/
param=15=0
param=12=255
param=10=5
   ```
#### proxy-targets file

1. Now we need to tell the proxy where to go. Add a new file named `proxy-targets.yaml` at the user directory `C:\Users\User\.rsprox`
2. Paste the following info into the file. Be sure to update the modulus to your new server build and change the config link to where the config URL is hosted:
```yaml
    config:
  - name: Alter
    jav_config_url: https://github.com/Auspicious-Rook/rsps/raw/refs/heads/main/jav.ws
    varp_count: 15000
    revision: 228.2
    modulus: b18779e8230af435c878a3e3f0a8601153646830b5c2c6c15c31275b95c616b841545e54836768ab3fc4bb19a0f939cbd32f88c7c2e108b517cd561577a9cb61f9d6b3de88401e94ec2b3da78b463bf6fdae583b67d6520bdbe7421864836f60ee2e76aa056c797375dc39995a11b21673bc7ae7037bde9d8499287526ab2811
    runelite_bootstrap_commithash: 6e7c6956aa4d9f4c30349182f57ccd86d63ae57d
    runelite_gamepack_url: https://github.com/runetech/osrs-gamepacks/raw/refs/heads/master/gamepacks/osrs-228.jar
```

### Game time!!!
1. Alright, now we are cookin. TIme to login for real.
2. Run this command in the rsprox intellij terminal or just run the stand alone `.exe`:  `./gradlew proxy`
3. If your server is still up, firewalls adjusted, and files created correctly, you should see your own server. Log in with whatever name and password you want, but remember them for next time. 
4. Enjoy!!!
