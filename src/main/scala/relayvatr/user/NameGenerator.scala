package relayvatr.user

import scala.util.Random

object NameGenerator {

  val names = Set(
    "James", "David", "Christopher", "George", "Ronald", "John", "Richard",
    "Daniel", "Kenneth", "Anthony", "Robert", "Charles", "Paul", "Steven",
    "Kevin", "Michael", "Joseph", "Mark", "Edward", "Jason", "William", "Thomas",
    "Donald", "Brian", "Jeff", "Mary", "Jennifer", "Lisa", "Sandra", "Michelle",
    "Patricia", "Maria", "Nancy", "Donna", "Laura", "Linda", "Susan", "Karen",
    "Carol", "Sarah", "Barbara", "Margaret", "Betty", "Ruth", "Kimberly",
    "Elizabeth", "Dorothy", "Helen", "Sharon", "Deborah")

  val surnames = Set(
    "Volga", "Danube", "Ural", "Dnieper", "Don", "Pechora", "Kama", "Northern",
    "Oka", "Belaya", "Dniester", "Rhine", "Elbe", "Donets", "Vistula", "Tagus",
    "Daugava", "Loire", "Tisza", "Prut", "Sava", "Neman", "Meuse", "Ebro", "Douro",
    "Kuban", "Mezen", "Oder", "Rhone", "Mures", "Seine", "Gota", "Drava", "Guadiana",
    "Siret", "Po", "Guadalquivir", "Glomma", "Olt", "Neva–Svir–Suna", "Moselle",
    "Torne", "Dal", "Maritsa", "Lule", "Ume", "Angerman", "Kalix", "Ljusnan",
    "Indal", "Ialomita", "Struma", "Adige", "Skellefte", "Tiber", "Vah", "Pite",
    "Somes", "Iskar", "River", "River", "Arges", "Tundzha", "Thames", "Drina",
    "Jiu", "Timis", "Drin", "Haliacmon", "Ljungan", "Morava", "Kupa", "Dambovita",
    "Yantra", "Bistrita", "Jijia", "Vjose", "Bosna", "Bega", "Kamchiya", "Lagan",
    "Arno", "Atran", "Vrbas", "Mondego", "Neretva", "Nestos", "Vedea", "Achelous",
    "Pineios", "Crna", "Nissan")

  def newName(): String =
    Seq(pick(names), pick(surnames)).mkString(" ")

  private def pick(options: Set[String]): String = {
    Random.shuffle(options.toList).head
  }

}
