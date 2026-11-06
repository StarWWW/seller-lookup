# Seller Lookup

Açık artırmada bir ilanın satıcısına tek tuşla bak: bir sunucunun `/ah` ekranında fareyle bir ilanın üstüne gel ve **Shift**'e basılı tut. Ekran kapanır ve sohbet, satıcının adıyla yazılmış bir komutla açılır: Shift ile `/ah <satıcı>`, Ctrl ile `/stats <satıcı>`. Komut gönderilmez, Enter'a basan sensin.

*English: A client-side Fabric mod for Minecraft 26.1.2, 26.2 and 26.3. Hold Shift (or Ctrl) over a listing in a server's auction house: the screen closes and the chat opens with a command about the seller already typed, such as `/ah <seller>` or `/stats <seller>`. Nothing is sent until you press Enter.*

## Kullanım

1. `/ah` ekranını aç ve fareyle bir ilanın üstüne gel. İlanın satıcısı okunabiliyorsa ipucunun altında ne yazılacağı görünür:
   ```
   Shift basılı tut: /ah Notch
   Ctrl basılı tut: /stats Notch
   ```
2. **Shift**'e basılı tut (varsayılan 0,4 saniye): `/ah` ekranı kapanır, sohbet `/ah Notch` yazılı açılır. **Ctrl** ile `/stats Notch`.
3. Enter'a bas ya da komutu değiştir; vazgeçersen Esc.

Kısa bir Shift basışı ya da Shift'le tıklama (shift-click) eskisi gibi çalışır: komut sadece tuş, aynı ilanın üstünde süre dolana kadar basılı kalınca yazılır, Shift basılıyken tıklamak o basışı iptal eder. Fare başka bir satıcının ilanına geçerse süre yeniden başlar.

Ekran, sunucuya da kapatıldığı söylenerek kapanır (Esc ile kapatmak gibi).

Kısayol sadece sandık benzeri ekranlarda değil, satıcısı yazan bir eşya gösteren her ekranda çalışır (sohbet hariç), sunucunun açtığı onay pencereleri de buna dahil. Ekrandaki bir yazı kutusuna yazı yazarken Shift büyük harf içindir, o sırada kısayol çalışmaz.

### DonutSMP

DonutSMP'de `/ah` listesindeki ilanlarda sadece fiyat yazar (`$ 100M`), satıcı yazmaz; o yüzden listede ipucu satırı çıkmaz ve Shift bir şey yapmaz. Satıcı, ilana **sağ tıklayınca** açılan onay penceresinde ("Are you sure you want to buy this?") yazar:

1. `/ah`'da ilana sağ tıkla.
2. Açılan pencerede fareyle eşyanın üstüne gel: ipucunda `Seller: _Restrict` ve altında `Shift basılı tut: /ah _Restrict` görünür.
3. Shift'e (ya da Ctrl'ye) basılı tut: pencere kapanır, sohbet komut yazılı açılır. Mod hiçbir şeye tıklamaz; pencerede **Yes**'e basmadıkça bir şey satın alınmaz.

### Satıcı nasıl okunuyor

İlanın altındaki satırlarda `Seller`, `Satıcı`, `Owner`, `Sahibi`, `Sold by`, `Player` gibi bir kelime aranır ve ondan sonra gelen ilk oyuncu adı alınır. Rütbeler (`[VIP]`, `[+]`) atlanır, Bedrock adlarının başındaki `.` ya da `*` korunur. Ad bir sonraki satırdaysa (ör. `Seller:` ve altında `Notch`) o da okunur:

```
Seller: [VIP] Notch      ->  Notch
Satıcı » .BedrockAdam    ->  .BedrockAdam
```

Sunucunun yazımı farklıysa ve satıcı okunmuyorsa ayarlara (ya da `config/sellerlookup.properties` içine), ilk grubu satıcının adını veren kendi düzenli ifadeni yazabilirsin:

```
sellerPattern=Satan\: (\S+)
```

### Ayarlar

Mod Menu'den ya da Kontroller > Seller Lookup'tan atayabileceğin tuşla açılır (varsayılan olarak boştur):

- **Genel > Açık:** kısayolun tamamını kapatıp açar.
- **Genel > İpucunda göster:** ilanın altındaki `Shift basılı tut: ...` satırları; satıcının doğru okunduğunu basmadan görürsün.
- **Genel > Basılı tut** (0-1500 ms, 50 ms adımlarla, varsayılan 400): kısa basışların ve shift-click'lerin karışmaması için.
- **Shift basılı tut > Komut** ve **Ctrl basılı tut > Komut:** sohbete yazılacak komut; `{seller}` satıcının adıyla değişir. Boş bırakırsan o tuş kapanır. Örnek: `/msg {seller} ilanın hâlâ satılık mı?`, `/pay {seller}`.
- **Varsayılanlar:** ayarları ilk hâline döndürür.

## Sunucularda

Mod sadece sunucunun zaten senin istemcine gönderdiği ilan satırlarını okur ve sohbete bir komut yazar; hiçbir şeyi kendisi göndermez. Yine de oynadığın sunucunun (ör. DonutSMP) istemci modlarıyla ilgili kurallarına bak.

## Gereksinimler

- Minecraft 26.1.2 (26.1.x), 26.2 veya 26.3, Fabric Loader 0.19+, Java 25
- Fabric API (26.1.2 için 0.155.3+, 26.2 için 0.161.0+, 26.3 için 0.163.0+)
- İsteğe bağlı: Mod Menu

## Derleme

Java 25 gerekir. Prism Launcher'ın indirdiği Java 25'i de kullanabilirsin:

```
set JAVA_HOME=%APPDATA%\PrismLauncher\java\java-runtime-epsilon
gradlew build
```

Bu Minecraft 26.1.2 jar'ını üretir. 26.2 ve 26.3 için:

```
gradlew build -Pmc=26.2
gradlew build -Pmc=26.3
```

Hazır jar'lar `build/libs/` klasörüne çıkar (`-sources` olmayan dosya), adında Minecraft sürümü yazar: `sellerlookup-<sürüm>+26.1.2.jar`, `sellerlookup-<sürüm>+26.2.jar` ve `sellerlookup-<sürüm>+26.3.jar`. Her derleme jar'ı ayrıca ana klasördeki `builds/<minecraft sürümü>/` klasörüne kopyalar ve oradaki eski sürümünü siler. `gradlew build` ayrıca `src/test` altındaki JUnit testlerini de çalıştırır.

Her sürümün bağımlılık sürümleri `versions/<minecraft>.properties` dosyasındadır. Minecraft sürümleri arasında farklı olan birkaç çağrı (26.2'den itibaren ekranların `Minecraft.gui` üzerinden açılması gibi) `src/mc26.1/java` ve `src/mc26.2/java` altındaki `Compat` sınıfındadır; 26.3 26.2'nin kodunu kullanır. Shift ve Ctrl, sabit tuş kodları yerine oyunun tuş adlarıyla (`key.keyboard.left.shift`) bulunur, çünkü 26.3'te tuş kodları değişti.

## Proje yapısı

```
src/main/java/sellerlookup/
  SellerLookupMod.java          giriş noktası, ayar tuşu
  Seller.java                   ilan satırlarından satıcıyı okur, komutu yazar (oyundan bağımsız, test edilebilir)
  SellerLookupConfig.java       ayarlar (config/sellerlookup.properties)
  client/ListingShortcut        ipucu satırları, Shift/Ctrl'ye basılı tutma, ekranı kapatıp sohbeti açma
  client/SellerLookupScreen     ayar ekranı
  client/ui/                    ayar ekranı iskeleti; diğer modlarla aynıdır
  compat/ModMenuIntegration
src/mc26.1/java, src/mc26.2/java   Minecraft sürümüne göre değişen birkaç çağrı (Compat)
versions/                      her Minecraft sürümü için bağımlılık sürümleri
src/main/resources/            fabric.mod.json, dil dosyaları (19 dil), ikon
src/test/java/sellerlookup/    JUnit testleri (satıcı okuma, dil dosyaları)
tools/MakeIcon.java            logoyu üretir (AWT'li bir Java ile: javac tools/MakeIcon.java, java -cp tools MakeIcon)
```

## Ayar dosyası

`config/sellerlookup.properties`:

```
enabled=true
tooltipHint=true
holdMs=400
shiftCommand=/ah {seller}
ctrlCommand=/stats {seller}
sellerPattern=
```

## Lisans

MIT, `LICENSE` dosyasına bak.
