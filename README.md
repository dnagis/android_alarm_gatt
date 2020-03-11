AlarmGatt

Avoir du Gatt persistent. En rando, en kite à Bauduc (aile ou anémo), dans une cabane en Ariège...


Stub -> HelloActivity
rm -r test/ (ou tests?)
	android.mk local_package_name
	manifest: nom du package, nom de l'activité
	.java: nom de la classe, nom du package dans la classe
	on peut très bien avoir un nom de package/directories java très court
	res/layout -> nom de l'activité <mon_activity>, celle qui est appelée dans 
	getLayoutInflater().inflate(R.layout.<mon_activity>, null);

make AlrmGatt
adb install out/target/product/mido/system/app/AlrmGatt/AlrmGatt.apk
adb uninstall vvnx.alrmgatt

WorkFlow

Démarrer avec une UI. Il me faut du feed back sur le terrain (si la connection se fait pas, s'il y a plus de batterie)

-> samples/Alarm
c'est du setRepeating. Ya pas de raison que ça résiste au Doze

-> alarmmanager setAndAllowWhileIdle(). Ca doit résister à l'Idle normalement.
Ce qu'il faut que je démystifie c'est le mécanisme: comment générer une nouvelle alarme une fois que l'ancienne a été triggered. 

Commencer par set une alarm? 


