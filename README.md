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



### WorkFlow


-Première branche sur laquelle j'ai travaillé (140320): AllowWhileIdle (mergée en master):
	Design: l'activité principale btn->starService(AlarmReceiver) > onCreate(), onStartCommand(), où du log en bdd est fait, et une alarm est settée
	via setAndAllowWhileIdle() avec un intent qui démarre AlarmReceiver.class (donc décla en manifest). C'est donc possible qu'un service 
	set une alarme pour se réveiller lui même. A la fin de onStartCommand j'ai mis stopSelf() et return START_NOT_STICKY.
	Fonctionnement: pas archi régulier (il y a un setexactandallowwhileidle que j'ai pas testé) mais persistant overnight. Si ITVL=5 min on passe souvent à 9 minutes
	, a l'air de fonctionner même quand, l'UI n'est pas on top (retour écran accueil)..
	
-Deuxième branche: "gatt" 




Les idées au cas où non persistent:
	Essayer de mettre une notification dans le service déclenché, comme dans samples/alarm, peut être que ça permet de passer outre les restrictions de batterie?
	Foregroune service




