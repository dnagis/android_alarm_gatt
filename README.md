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
	Premiers arrêts de l'appli, délai environ une heure. Mais je passe l'appli pas en foreground (retour home screen).
	dumpsys deviceidle -> idle_to=+1h0m0s0ms
	Hypothèse de ce kill: ce serait App Standby, qui expliquerait que mon appli soit killée quand pas en foreground.
	Je m'assure de pouvoir distinguer une appli killée d'un bluetooth pas récupéré -> log en bdd, même si bluetooth a foiré.
	
	
		Tester. 
			ITVL à 1 min
			Appli lancée, foreground vs. pas foreground, et utiliser une des méthodes de test adb:
				dumpsys deviceidle force-idle -> intervalle entre 2 logs passe à 9 minutes.
		
		Si c'est foreground: contourner avec la technique: "The app generates a notification that users see on the lock screen or in the notification tray." du dev guide doze-standby
	



Les idées au cas où non persistent:
	Essayer de mettre une notification dans le service déclenché, comme dans samples/alarm, peut être que ça permet de passer outre les restrictions de batterie?
	Foreground service




