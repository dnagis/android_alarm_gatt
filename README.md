# AlrmGatt --> persistence d'une connexion Gatt au long cours (plusieurs jours)

Une Alarm (AlarmManager) qui déclenche du Gatt. 
Motivation première: persistence au Doze - Idle (anémo, la nuit en rando bivouaque, home sans rpi, ...)

Comportement: Stable si l'UI est on top, et quand l'UI est en background (retour home screen) ou virée (enlevée de la LRU) si dumpsys deviceidle whitelist +vvnx.alrmgatt

Depuis j'ai trouvé de la doc doze-idle + réglages device idle en shell. voir en haut de morphotox/android

### Design
Une activity UI bouton qui startService un service "alarmReceiver".
Ce service s'auto set une alarm dans son onStartCommand() avec setAndAllowWhileIdle().
Au onResume() rafraichissement d'un text sur l'UI pour afficher dernier log.


### Comportement

Premiers tests
	-> Alarm sera effectivement firée en idle MAIS si et seulement si l'appli est on top (foreground) ***SAUF***
		si tu as fait un dumpsys deviceidle whitelist +vvnx.alrmgatt: Seule solution à l'heure actuelle pour persistance quand UI pas on top
	Attention le whitelist peut parfois être éphémère
	Je pensais que ça tenait sauf au reboot mais pas toujours vrai
	si UI pas on top et pas whitelist, au déclenchement de l'alarm: ActivityManager: Background start not allowed... Mettre des notifications n'y fait rien. 
	NB: ce système ne tient que sur des alarmes: c'est le seul link entre deux starts de mon service. Pas de déclenchement d'alarme ==> fin.
	Fréquence: en idle jamais < 9 min d'intervalle, en deep idle dans les maintenances 1 à 2 * / h. Consistent avec la doc Android.
	
	
Une grosse semaine au Thor
	Stable 1 à 2j
	La batterie du tel ne pose pas de problème
	MAIS 
	Problème de longévité de mes esp32 sur batterie. (nb: je ne faisais pas sleeper mes esp32). Au moins à 3 reprises au bout de 2/3 jours l'esp32 n'est plus visible avec hcitool lescan, et plus de nouvelles
	mesures récupérées.
	Je n'ai pas fait le test avec un esp32 sur secteur. 
	Problème pour relancer cette appli après qu'elle ait fonctionné initialement avec un esp32 qui s'est arrêté. Elle ne le fait pas automatiquement (pourtant il devrait 
	y avoir des alarmes? je n'ai pas regardé). J'ai réussi à la faire repartir une fois	en désactivant le bluetooth et en le réactivant, puis en relancant le service (bouton start).
	
	
### ToDo pour augmenter longévité (250320)
Tester avec un capteur sur secteur pour discerner problème Android / esp32
Tester comportement quand disparition / réapparition du capteur
Regarder dans la liste des alarmes si lorsque le capteur disparait les alarmes continuent à se setter ou disparition (en gros est ce que ce système
	résiste à l'absence de connexion
Essayer avec un esp32 qui sleep une partie du temps / adapter mon timeout? (Pas pour cette appli, pour la longévité du système en général)






### Ergonomie: EveryDay
# build & install

make AlrmGatt
adb uninstall vvnx.alrmgatt
adb install out/target/product/mido/system/app/AlrmGatt/AlrmGatt.apk



# repo / rsync
je commence à en avoir marre de git push à chaque fois donc du rsync vers kimsufi quand je travaille activement dessus
rsync options source destination
You can think of a trailing / on a source as meaning "copy the contents of this directory" as opposed to "copy the directory by name",
	but in both cases the attributes of the containing directory are transferred to the containing directory on the destination
-u: update: skip files that are newer on the receiver #mon mécanisme de protection "cook-proof" contre la connerie: overwriter des fichiers plus récents

rsync -azvhu /initrd/mnt/dev_save/android/lineageOS/sources/development/samples/AlarmGattVvnx ks:/home/android	
rsync -azvhu ks:/home/android/AlarmGattVvnx /initrd/mnt/dev_save/android/lineageOS/sources/development/samples


### Doc
https://developer.android.com/training/monitoring-device-state/doze-standby
https://forum.xda-developers.com/android/apps-games/root-doze-settings-editor-android-t3235130
frameworks/base/services/core/java/com/android/server/DeviceIdleController.java

### LOG

-Première branche sur laquelle j'ai travaillé (140320): AllowWhileIdle (mergée en master):
	Design: l'activité principale btn->starService(AlarmReceiver) > onCreate(), onStartCommand(), où du log en bdd est fait, et une alarm est settée
		via setAndAllowWhileIdle() avec un intent qui démarre AlarmReceiver.class (donc décla en manifest). 
	C'est donc possible qu'un service set une alarm pour se réveiller lui même. A la fin de onStartCommand j'ai mis stopSelf() et return START_NOT_STICKY.
	Fonctionnement: persistant overnight. 
	Avec intervalle  à 5 min on passe souvent à 9 minutes -> cohérent avec Android dev guide:
		"Note: Neither setAndAllowWhileIdle() nor setExactAndAllowWhileIdle() can fire alarms more than once per 9 minutes, per app."
	, a l'air de fonctionner même quand, l'UI n'est pas foreground (retour écran accueil) -> si c'est vrai: très différent avec le comportement quand bluetooth
	
-Deuxième branche: "gatt" 
	Bien distinguer les problèmes bluetooth des problèmes idle: -> log en bdd, dans onDestroy(), même si bluetooth a foiré.
	Je m'assure de pouvoir distinguer une appli killée d'un bluetooth pas récupéré 	
		--> j'ai parfois des connections fantômes! readChar ne passe pas, et si je reset l'esp32 à ce moment là, le logcat me montre que plein de connections gatt attendaient
		--> je modifie donc le code pour n'avoir qu'une seule instance de BluetoothGatt, que je reconnecte à chaque fois.

	
	dumpsys deviceidle -> les settings affichés. (idle_to=+1h0m0s0ms ...), et l'historique (light idle, deep idle, ...)

	Pour tester idle : dumpsys deviceidle force-idle -> intervalle entre 2 logs passe à 9 minutes.

	Comportement actuel le 1703, téléphone débranché non touché: 
		avec alarm set via setAndAllowWhileIdle, et un gatt qui est créé la première fois puis se reconnecte à chaque fois, et appli UI en foreground, pas de notifs ni de foreground service:
		Stabilité: pas de plantage, on passe en idle dès débranchement: avec ITVL demandé 1 min: pendant une heure un réveil toutes les 10 minutes, puis deep idle, et là variable: va de 1 à 2 réveil par heure.
			récup de readChar à chaque fois.
		Je n'ai pas noté combien de batterie consommée par nuit. Pas fait de multiples nuits non plus.
		Si UI en background: arrêt de l'appli
		
-Entre deuxième et troisième branche je remets le log de la batterie

	dev guide doze-standby: Dit qu'on peut contourner avec la technique: "The app generates a notification that users see on the lock screen or in the notification tray." 		

-Fait une branche notif, y compris avec du startForeground	->  ne m'a pas résolu le pb foreground / background

## Résolution du problème: "quand l'UI n'est pas on top l'appli s'arrête". In a nutshell: quand l'appli n'est pas en foreground c'est l'activitymanager qui "ActivityManager: Background start not allowed".
	solution: dumpsys deviceidle whitelist +vvnx.alrmgatt
	
	
	Les intervenants potentiels:
	-deviceidle	- dumpsys deviceidle
	-alarmmanager - dumpsys alarm
	-activity manager - dumpsys activity
	
Dumpsys activity - h
	dumpsys activity -p vvnx.alrmgatt
	dumpsys activity package vvnx.alrmgatt
	dumpsys activity -p vvnx.alrmgatt processes
		->  *APP* UID 10294 ProcessRecord{3b68530 15030:vvnx.alrmgatt/u0a294}
		->  Process LRU (Least Recently Used) list -> top activity, previous, je comprends à peu près.
		-> 	google 'LRU' -> https://developer.android.com/guide/components/activities/process-lifecycle
	
Dumpsys alarm
	mAllowWhileIdleMinTime=+9m0s0ms <- 9min! correspond à ce qui est écrit dans le dev guide
	min_interval=+1m0s0ms <- tient c'est ça qui me bloque à 60 secondes minimum ??? réglable?
	
Hypothèses:
	premier lancement: on réveille toutes les minutes, et à partir du moment où j'ai passé l'UI derrière: c'est fini j'aurai jamais plus une minute. 
		Question: si c'est bien ça, ça se passe où cette décision de 'delay' l'alarm???
		
	même si je vire l'UI, l'alarm reste, mais:

Si mon UI est pas on top (soit diminuée, soit arrêtée) et que j'attends une alarm (dumpsys alarm me dit les pending alarms) au moment où elle devrait être lancée je vois:
03-18 10:19:59.981  1322  1570 W ActivityManager: Background start not allowed: service Intent { flg=0x4 cmp=vvnx.alrmgatt/.AlarmReceiver (has extras) } to vvnx.alrmgatt/.AlarmReceiver from pid=-1 uid=10294 pkg=vvnx.alrmgatt

grep de l'erreur:
frameworks/base/services/core/java/com/android/server/am/ActiveServices.java

mido / # am start-service vvnx.alrmgatt/.AlarmReceiver
Starting service: Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=vvnx.alrmgatt/.AlarmReceiver }
Error: app is in background uid UidRecord{e6adf73 u0a294 CAC  bg:+15m26s630ms idle change:idle procs:1 seq(0,0,0)}

dumpsys deviceidle whitelist +vvnx.alrmgatt

-Branche UI
	Un peu fourre tout. Affichage de la dernière entrée, BME280, ...

