# AlrmGatt


Avoir du Gatt déclenché par Alrm, persistent. 
En montagne (bivouaque, marche, grandeVoie, ...), en kite (anémo, capteurs bio pendant nav pour détecter problème, ...), dans une cabane en Ariège (température + Pression la nuit, ...),
	dans un hôtel à Kaboul (PIR, détecteur de fumée, ...), ...
UI (pas de démarage en shell) -> Il me faut du feed back sur le terrain (si la connection se fait pas, s'il y a plus de batterie)


### EveryDay
make AlrmGatt
adb install out/target/product/mido/system/app/AlrmGatt/AlrmGatt.apk
adb uninstall vvnx.alrmgatt

### rsync
rsync options source destination
You can think of a trailing / on a source as meaning "copy the contents of this directory" as opposed to "copy the directory by name",
	but in both cases the attributes of the containing directory are transferred to the containing directory on the destination
-u: update: skip files that are newer on the receiver #mon mécanisme de protection "cook-proof" contre la connerie: overwriter des fichiers plus récents
	
rsync -azvhu ks:/home/android/AlarmGattVvnx /initrd/mnt/dev_save/android/lineageOS/sources/development/samples
rsync -azvhu /initrd/mnt/dev_save/android/lineageOS/sources/development/samples/AlarmGattVvnx ks:/home/android

### Construction de l'ébauche - stub
sample/HelloActivity
rm -r tests
	android.mk local_package_name
	manifest: nom du package, nom de l'activité
	.java: nom de la classe, nom du package dans la classe
	on peut très bien avoir un nom de package/directories java très court (src/vvnx/maClasse.java)
	res/layout -> nom de l'activité <mon_activity>, celle qui est appelée dans 
	getLayoutInflater().inflate(R.layout.<mon_activity>, null);



### WorkFlow

samples/Alarm
c'est du setRepeating. Ya pas de raison que ça résiste au Doze

Stratégies à tester:

-Essayer set[exact]AndAllowWhileIdle()
Démystifier le mécanisme: est ce que je peux set une nouvelle alarm from within celle qui vient de fire off?
j'ai de l'espoir avec:
https://stackoverflow.com/questions/11123543/alarmmanager-repeat
https://code.luasoftware.com/tutorials/android/android-alarmmanager-repeating-alarm-daily-reminder/

Les idées au cas où ça ne marche pas:
	Essayer de mettre une notification dans le service déclenché, comme dans samples/alarm, peut être que ça permet de passer outre les restrictions de batterie?

-Faire comme LocTrack:
	la main activity:
		setRepeating qui déclenche une classe qui extends service qui fait le job, et write dans une BDD
		lance un foreground service (avec ses notifs)


