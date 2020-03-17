# AlrmGatt

Avoir du Gatt déclenché par Alrm, persistent. 
En montagne (bivouaque, marche, grandeVoie, ...), en kite (anémo, capteurs bio pendant nav pour détecter problème, ...), dans une cabane en Ariège (température + Pression la nuit, ...),
	dans un hôtel à Kaboul (PIR, détecteur de fumée, ...), ...
UI (pas de démarage en shell) -> Il me faut du feed back sur le terrain (si la connection se fait pas, s'il y a plus de batterie)


### EveryDay
# build & install
make AlrmGatt
adb install out/target/product/mido/system/app/AlrmGatt/AlrmGatt.apk
adb uninstall vvnx.alrmgatt

# rsync
rsync options source destination
You can think of a trailing / on a source as meaning "copy the contents of this directory" as opposed to "copy the directory by name",
	but in both cases the attributes of the containing directory are transferred to the containing directory on the destination
-u: update: skip files that are newer on the receiver #mon mécanisme de protection "cook-proof" contre la connerie: overwriter des fichiers plus récents
	
rsync -azvhu ks:/home/android/AlarmGattVvnx /initrd/mnt/dev_save/android/lineageOS/sources/development/samples
rsync -azvhu /initrd/mnt/dev_save/android/lineageOS/sources/development/samples/AlarmGattVvnx ks:/home/android

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

		

notes en vrac pour la suite:
	Appli pas en foreground (retour home screen) modifie le comportement quand tel débranché. kill je dirais...
		Hypothèse: ce serait App Standby, qui expliquerait que mon appli soit killée quand pas en foreground.	
	dev guide doze-standby:
	Dit qu'on peut contourner avec la technique: "The app generates a notification that users see on the lock screen or in the notification tray." 
			dans samples/alarm, il y a de la notif
		
ToDo:	
		Notif, pour ne pas être obligé d'avoir l'UI de l'appli en foreground
		Foreground service?	
		foreground vs. pas foreground, dev guide doze/app_standby parle de 
