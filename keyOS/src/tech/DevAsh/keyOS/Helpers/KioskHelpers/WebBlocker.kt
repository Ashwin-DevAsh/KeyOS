package tech.DevAsh.keyOS.Helpers.KioskHelpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import tech.DevAsh.KeyOS.Database.UserContext
import java.net.URL
import java.util.*

object WebBlocker {

    var context:Context?=null

    fun block(event: AccessibilityEvent, _context: Context){
        context=_context
        val packageName = event?.packageName?.toString()
        var browserConfig: SupportedBrowserConfig? = null
        for (supportedConfig in getSupportedBrowsers()) {
            if (supportedConfig.packageName == packageName) {
                browserConfig = supportedConfig
            }
        }

        if (browserConfig == null) {
            return
        }

        val parentNodeInfo = event.source ?: return


        val capturedUrl = captureUrl(parentNodeInfo, browserConfig)
        parentNodeInfo.recycle()

        //we can't find a url. Browser either was updated or opened page without url text field
        if (capturedUrl == null) {
            return
        }

        val eventTime = event.eventTime
        val detectionId = "$packageName, and url $capturedUrl"
        val lastRecordedTime = if (previousUrlDetections.containsKey(
                        detectionId)) previousUrlDetections[detectionId]!! else 0.toLong()
        //some kind of redirect throttling
        if (eventTime - lastRecordedTime > 2000) {
            previousUrlDetections[detectionId] = eventTime
            analyzeCapturedUrl(capturedUrl, browserConfig.packageName)
        }
    }

    private fun analyzeCapturedUrl(capturedUrl: String, browserPackage: String) {
        val redirectUrl = "about:blank"
        if (!isAllowedSite(capturedUrl)) {
            performRedirect(redirectUrl, browserPackage)
        }
    }

    private fun performRedirect(redirectUrl: String, browserPackage: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            intent.setPackage(browserPackage)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackage)
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
           context?. startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }


    private class SupportedBrowserConfig(var packageName: String, var addressBarId: String)


    private fun getSupportedBrowsers(): List<SupportedBrowserConfig> {
        val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
        browsers.add(SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"))
        browsers.add(SupportedBrowserConfig("org.mozilla.firefox",
                                            "org.mozilla.firefox:id/url_bar_title"))
        browsers.add(SupportedBrowserConfig("com.brave.browser", "com.brave.browser:id/url_bar"))
        browsers.add(SupportedBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"))
        browsers.add(SupportedBrowserConfig("com.duckduckgo.mobile.android",
                                            "com.duckduckgo.mobile.android:id/omnibarTextInput"))
        return browsers
    }

    private val previousUrlDetections: HashMap<String, Long> = HashMap()


    private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        if (nodes == null || nodes.size <= 0) {
            return null
        }
        val addressBarNodeInfo = nodes[0]
        var url: String? = null
        if (addressBarNodeInfo.text != null && !addressBarNodeInfo.isFocused) {
            url = addressBarNodeInfo.text.toString()
        }
        addressBarNodeInfo.recycle()
        return url
    }

    private fun isAllowedSite(urlString: String):Boolean{


        println("urlString = $urlString")

        if(!UserContext.user!!.webFilter.isEnabled){
            return true
        }

        try{

            if(urlString.contains("about:blank")){
                return true
            }

        val url = URL("https://$urlString")
        var host: String =  url.host
        val count: Int = host.length - host.replace(".", "").length
        if( count>1){
            host = host.substring(host.indexOf(".") + 1)
        }


        println("host = $host")

        if(UserContext.user!!.webFilter.isWhitelistEnabled){
            return UserContext.user!!.webFilter.whitelistWebsites.contains(host)
        }

        if(UserContext.user!!.webFilter.isBlacklistEnabled){
           return !UserContext.user!!.webFilter.blacklistWebsites.contains(host)
        }

        if(UserContext.user!!.webFilter.shouldBlockAdultSites){
            return !pornSites.contains(host)
        }



        return false
        }catch (e:Throwable){
            return true
        }
    }


    var pornSites = arrayListOf("indlansex.net","3rat.com","4hen.com","africansexvideos.net","bananabunny.com","cutepornvideos.com","desimurga.com","desisexclips.com","dslady.com","eroticperfection.com","es.porn.com","gaypornium.com","gracefulnudes.com","hot-dates.info","hqlinks.net","how-do-you-produce-more-seminal-fluid.semenaxx.org","indianporntube.xxx","indiansex4u.com","jav-porn.net","kirtu.com","legalporno.com","luboeporno.com","mypornbookmarks.com","pinkythekinky.com","pornfromczech.com","sexsex.hu","sexxxxi.com","shemale.asia","teengayporntube.com","thefreecamsecret.com","theporndude.com","momsteachsex.com","videos.petardas.com","xvideos.com","89.com","adultsextube.com","alohatube.com","analsexstars.com","babosas.com","bomnporn.com","brazzers.com","callboyindia.com","en.cam4.co","en.cam4.com.br","cam4.in","cholotube.com","cliphunter.com","cullosgratis.com.ve","cumlouder.com","darering.com","drtuber.com","epicporntube.com","eporner.com","fapto.xxx","flirt4free.com","freeones.com","freshporn.info","fuckcuck.com","gracefulnudes.com","gayboystube.com","fuq.com","hairy.com","hindisex.com","iknowthatgirl.com","indianpornovid.com","indianpornvideos.com","ixxx-tube.com","ixxx.com","ixxx.com.es","ixxx.ws","jizzhut.com","labatidora.net","leche69.com","livjasmin.com","locasporfollar.com","lushstories.com","mc-nudes.com","milfmovs.com","myfreecams.com","naughty.com","penguinvids.com","perfectgirls.net","perucaseras.com","pinkworld.com","playboy.com","playvid.com","pornhub.com","porno.com","pornorc.net","porntube.com","puritanas.com","redtube.com","rk.com","roundandbrown.com","serviporno.com","sexocean.com","teenpornxxx.net","thefreecamsecret.com","tnaflix.com","truthordarepics.com","tube8.com","tubegalore.com","videosdemadurasx.com","watchmygf.com","x-ho.com","xixx.com","xnxx.com","xtube.com","xvideosnacional.com","xxx.com","youjizz.com","youporn.com","xhamster.com","xhot.sextgem.com","xxx.com","jeux-flash-sexy.com","purebbwtube.com","babes.com","fotomujeres.pibones.com","rubber-kingdom.com","savitabhabhi.mobi","pinkvisualtgp.com","antarvasna.com","hot-gifz.com","lechecallente.com","parejasfollando.es","flirthookup.com","cerdas.com","es.chaturbate.com","youngpornvideos.com","nudevista.com","2gayboys.com","pornxxxtubes.com","ledauphine.com","freex.mobi","megavideoporno.org","pornochaud.com","gokabyle.com","bdenjoymore.blogspot.com","petardas.com","toroporno.com","conejox.com","sambaporno.com","voyeurpipi.com","porn.mangassex.com","goulnes.pornoxxxi.net","videos-x.xpornogays.com","indienne-sexy.com","arabebaise.com","ohasiatique.com","porn.com","xxxonxxx.com","sexxxdoll.com","xxxvideosex.org","gonzoxxxmovies.com","keezmovies.com","xxx.xxx","poringa.net","videosxxxputas.xxx","lisaannlovers11.tumblr.com","h33t.to","premiercastingporno.com","marocainenue.com","fr.perfectgirls.net","jeffdunhamfuckdoll.com","pornmotion.com","gorgeousladies.com","fille-nue-video.com","teensnow.com","theofficiallouisejenson.com","bangbros.com","yourather.com","bootlus.com","conejox.com","toonztube.com","top-chatroulette.com","videosfilleschaudes.com","fillechaude.com","femmesmuresx.net","liberteenage.com","coffetube.com","awesomeellalove.tumblr.com","xnxxgifs.com","gaygautemela.com","saoulbafjojo.com","pornofemmeblack.com","sexonapria.org","beurettehot.net","woodstockreborn.tumblr.com","freesex.com","peliculaspornogratisxxx.com","porno-algerienne.com","belles-femmes-arabes.blogspot.com","lesbiennesxxx.com","des-filles-sexy.com","videos-porno-chaudes.com","xgouines.com","couleurivoire.com","3animalsextube.com","moncotube.net","mouparkstreet.blogspot.com","sexocean.com","sexcoachapp.com","femdomecpire.com","babosas.co","guide-asie.com","beauxcul.com","maghrebinnes.xl.cx","axnxxx.org","xnxx-free.net","xnxx.vc","es.bravotube.net","femmesporno.com","tubeduporno.com","videos-sexe.1touffe.com","video-porno.videurdecouilles.com","rubias19.com","xxi.onxxille.com","asiatique-femme.com","masalopeblack.com","beautyandthebeard1.tumblr.com","beautiful-nude-teens-exposed.tumblr.com","porno-marocaine.com","69rueporno.com","fuckmycheatingslutwife.tumblr.com","arabe-sexy.com","film-porno-black.com","sexe-evbony.com","gratishentai.net","cochonnevideosx.com","chaudassedusexe.com","videosanalesx.com","pornotantique.com","dorceltv.xn.pl","video-sex.femmesx.net","boutique-sexy.ch","salope-marocaine.com","pornocolumbia.co","jeunette18.com","sexe2asiatique.com","redtuve.com","les-groses.net","nexxx.com","freesex.com","videospornonacional.com","xxxkinky.com","yasminramos.com","tukif.com","porno-wife.com","film-xxx-black.com","sex.com","every-seconds.tumblr.com","adultwork.com","hairy.com","tendance-lesbienne.com","jpangel101.tumblr.com","18teensexposed.tumblr.com","girthyencounters.tumblr.com","cuckinohio.tumblr.com","dildosatisfaction.tumblr.com","stretchedpussy.tumblr.com","mindslostinlust.tumblr.com","whoresmilfsdegraded.tumblr.com","bigdickswillingchicks.tumblr.com","indiansexstories.net","beeg.com","eros.com","brazzersnetwork.com","sextubelinks.com","xxxbunker.com","7dog.com","vivthomas.com","porn00.org","teensnowxvideos.com","x-art.com","chaturbate.com","pinkworld.com","pandamovies.com","muyzorras.com","videos-porno.x18xxx.com","uplust.com","shemales.com","bigboobsalert.com","culx.org","gay43.com","blogfalconstudios.com","store.falconstudios.com","premium.gays.com","omegaporno.com","specialgays.com","gggay.com","nautilix.com","ovideox.com","aztecaporno.com","hard.pornoxxl.org","xxl.sexgratuits.com","pornosfilms.com","herbalviagraworld.com","primecurves.com","xbabe.com","webpnudes.com","hornybook.com","pinsex.com","smutty.com","dreammovies.com","pornhubfillesalope.com","girlygifporn.com","arabicdancevideo.blogspot.com","kellydivine.co","tubepornstars.com","vintagehairy.net","lookatvintage.com","pornorama.com","ass4all.com","cindymovies.com","jizzle.com","onlygirlvideos.com","roflpot.com","spankwire.com","arabesexy.com","megamovie.us","nakedboobs.net","teencamvids.org","nudeboobshotpics.com","live.sugarbbw.com","sexbotbonnasse.com","popurls.com","salope.1japonsex.com","nudematurewomenphotos.com","eroticbeauties.net","milfs30.com","freshmatureporn.com","matureshine.com","wetmaturewhores.com","matures-photos.com","mature-galleries.org","owsmut.com","maturestation.com","webcam.com","maturelle.com","womenmaturepics.com","all-free-nude-old-granny-mature-women-xxx-porn-pics.com","maturepornhub.com","nudeold.com","uniquesexymoms.com","nude-oldies.com","riomature.com","hot-naked-milfs.com","stiflersmoms.com","multimature.com","oldhotmoms.com","matureoracle.com","hungrymatures.com","milfous.com","watchersweb.com","eromatures.net","mom50.com","grannyxxx.co.uk","maturesinstockings.com","imaturewomen.com","wetmaturewomen.com","matureandyoung.com","momshere.com","riomoms.com","kissmaturesgo.com","bitefaim.com","milfionaire.com","sexymaturethumbs.com","maturosexy.com","6mature9.com","hotnakedoldies.com","golden-moms.com","madmamas.com","womanolder.com","matureland.net","motherstits.com","unshavenpussies.net","pornmaturepics.com","105matures.com","momstaboo.com","broslingerie.com","elderly-women.com","upskirttop.net","bushypussies.net","amateurmaturewives.com","universeold.com","unshavengirls.net","oldernastybitches.com","maturewant.com","juliepost.com","mulligansmilfs.com","bestmaturewomen.com","riomature.com","mature-orgasm.com","inlovewithboobs.com","riotits.net","nakedbustytits.com","ass-butt.com","matureladiespics.com","pornmaturewomen.com","nudemomphotos.com","secinsurance.com","bigfreemature.com","mature-women-tube.net","hotnudematures.com","oldsexybabes.net","matureasspics.com","mature30plus.com","matureamour.com","themomsfucking.net","boobymilf.com","fantasticwomans.com","xxxmaturepost.com","alloldpics.com","lenawethole.com","mature.nl","wifezilla.com","chubbygalls.com","nudematurespics.com","matureal.com","thexmilf.com","cocomilfs.com","zmilfs.com","wild-matures.com","horny-matures.net","grandmabesttube.com","bestmilftube.com","needmilf.com","girlmature.com","bestmatureclips.com","lustfuloldies.com","riomoms.com","maturehotsex.com","bettermilfs.com","milfionaire.com","oldercherry.com","sexymilfpussy.com","maturepornpics.com","action36.com","dianapost.com","babesclub.net","lovely-mature.net","bestmaturesthumb.com","myfreemoms.com","milfatwork.net","milfgals.net","olderwomenarchive.com","milfmomspics.com","pornovideo.italy.com","stiflersmilfs.com","maturenags.com","maturenakedsluts.com","tgpmaturewoman.com","idealwifes.com","maturewitch.com","hqmaturemovs.com","mature-women-tube.org","olderwomentaboo.com","chocomilf.com","milfparanoia.com","momsnightjob.com","matureintros.com","booloo.com","bigbuttmature.com","maturetube.com","mature30-45.com","maturecool.com","mamitatube.com","freematurevideo.net","silkymoms.com","momsclan.com","bravomamas.com","sharedxpics.com","fuckmaturewhore.com","maturedummy.com","hotfreemilfs.com","el-ladies.com","xxxmomclips.com","idealmilf.com","alexmatures.com","kingsizebreasts.com","matureladies.com","bigtitsnaked.com","xebonygirls.com","numaturewomen.com","womeninyears.com","maturehere.com","milfpicshere.com","maturepicsarchive.com","viewmature.com","womenmaturepics.com","momspics.net","cleomture.com","milf-fucking.net","maturecherry.net","immoralmatures.com","pretty-matures.com","matureclithunter.com","ilovematurewomen.tumblr.com","nudematurepussy.com","nudemomandboy.com","mygranny.pics","eroticteens.pw","pussy-mature.com","fatsexygirls.net","40somethingmag.com","tgpmaturewoman.com","amazingmaturesluts.com","milftubevids.com","myhdshop.com","matureholes.net","vipoldies.net","juicy-matures.com","hotelmatures.com","gaytube.com","hardsexyyoupornhub.com","lewd-babes.com","xxx.adulttube.com","maturesexy.us","galsarchive.com","maturegirl.us","sexpics.xxx","mature-for-you.com","mulligansmilfs.com","gracefulmilf.com","momsforporn.com","sexyhotmilf.com","azgals.com","thematureladies.com","ahmilf.com","cheatwife.com","picsboob.com","agedmamas.com","bigtitsmilf.com","mturemomsporn.com","older-beauty.com","empflix.com","numoms.com","ladymom.com","ladymom.com","petiteporn.pw","grannyhairy.net","gramateurs.com","sexy-olders.com","fresh-galleries.com","nudematuremix.com","alansanal.com","mature-library.com","filthymamas.com","mature-beach.com","sexualolders.com","horny-olders.com","olderkiss.com","wethairywats.com","erotic-olders.com","maturemompics.com","maturedally.net","place21.com","teenhana.com","classic-moms.com","grandmammapics.com","xmilfpics.com","hotchicks.sexy","ebonyfantasies.com","milfbank.com","freematurepornpics.com","agedcunts.net","milfsection.met","myexmilf.com","bestmilfsporn.com","everydaycams.com","adultreviews.com","icematures.com","mature4.net","milfera.com","milfjam.com","bbwpornpics.com","pornxxx.com","milfkiss.com","chubbygirlpics.com","excitingmatures.com","hairymaturegirls.com","pamelapost.com","7feel.net","tubefellas.com","sexymaturethumbs.com","sexybuttpics.com","sexyhotmilfs.com","secretarypics.com","naked-moms.com","momhandjob.com","sexymaturethumbs.com","pornsticky.com","hairymilfpics.com","maturebrotherthumbs.com","hotmomsporn.com","nudematuremix.com","30plusgirls.com","wifesbank.com","milfsarea.com","pornoriver.net","milfsbeach.com","matureguide.com","dailyolders.com","askyourmommy.com","free-porn-pics.net","maturedolls.net","juicygranny.com","maturepornhere.com","nakedoldbabes.com","stripping-moms.com","sexymaturepussies.com","owerotica.com","old-vulva.com","oldmomstgp.com","posing-matures.com","momsecstasy.com","gracefulmom.com","wetmaturepics.com","wifenaked.net","maturexxxclipz.com","matureplace.com","riomilf.com","fresholders.com","hqoldies.com","bigtitsfree.net","amateur-libertins.net","maturepornqueens.net","amaclips.com","eroticplace.net","myonlyhd.com","amapics.net","30yomilf.com","fuckdc.com","mommyxxxmovies.com","teenpussy.pw","imomsex.com","matureandgranny.com","milffreepictures.com","xxxmaturepost.com","uniquesexymoms.com","fuckmaturewhore.com","gentlemoms.com","deviantclip.com","oldsweet.com","grannypornpics.net","lewdmistress.com","worldxxxphotos.com","sweetmaturepics.com","oldpoon.com","sexymaturepics.com","goodgrannypics.com","dagay.com","randyhags.com","thegranny.net","maturemomsex.com","maturesort.com","immodestmoms.com","immaturewomen.com","bigboty4free.com","tiny-cams.com","oldwomanface.com","home-madness.com","posingwomen.com","maturesensations.com","filthyoldies.com","matureclits.net","momsinporn.net","maturebabesporno.com","matureinlove.net","bigtitsporn.me","xxxolders.com","freemilfsite.com","sex.pornoxxl.org","queenofmature.com","hotmomspics.com","freemilfpornpics.com","ashleyrnadison.com","bizzzporno.com","sexy-links.net","hotsexyteensphotos.com","teemns-pic.com","video-porno.1lecheuse.com","mrskin.com","gobeurettes.com","actuallyattractiveamateurs.tumblr.com","sexynakedamateurgirls.com","nudedares.tumblr.com","amateur-sexys.tumblr.com","hothomemadepix.tumblr.com","hotamateurclip.com","voyeursport.com","upskirt.com","fakku.net","pornmirror.com","youjizz.ws","insext.net","pahubad.com","xtube.nom.co","boytikol.com","beeg.co","khu18.biz","gonzo.com","esseporn.com","myfreepornvideos.net","freeones.ch","efukt.com","newsfilter.org","xxxvideo.com","video-one.com","pornstarhangout.com","breeolson.com","porn720.com","collegehumor.com","barstoolsports.com","hollywoodjizz.com","shitbrix.com","xxxsummer.net","porny.com","video.freex.mobl","dixvi.com","pornnakedgirls.com","realitypassplus.com","digitalplayground.com","9gag.tv","kickass.com","es.xhamster.com","sex3.com","bravioteens.com","katestube.com","yourlust.com","wixvi.com","porntubevidz.com","3movs.com","buzzwok.com","largepontube.com","kickass.co","godao.com","hardsextube.com","ah-me.com","nuvid.com","10pointz.com","jrunk.tumblr.com","pornerbros.com","porndig.com","bigtinz.com","8nsex.com","imagefap.com","adultfriendfinder.com","pornodoido.com","hdrolet.com","xpornking.com","pornokutusu.com","pornzz.com","pornoforo.com","milfpornet.com","kink.com","squirtingmastery.com","thehotpics.com","pof.com","eatyouout.tumblr.com","playboy.com","milfsaffair.com","indiangilma.com","private.com","fuck-milf.com","foto-erotica.es","daultpornvideox.com","es.bongacams.com","ww.lastsexe.com","pinksofa.com","pinkcupid.com","onlyporngif.com","sexyono.com","shitbrix.com","motherless.com","thehotpics.com","joncjg.blogspot.in","fr-nostradamus.com","masturbationaddicton.net","japanesexxxtube.com","kilopics.com","find-best-lingerie.com","dustyporn.com","cleoteener.com","teen18ass.com","eternaldesire.com","sexyteensphotos.com","teenpornjoy.com","bubblebuttpics.com","allofteens.com","tinysolo.com","mynakedteens.com","youngmint.com","yourlustgirlfriends.com","youngxxxpics.com","pinkteenpics.com","clit7.com","find-best-videos.com","freekiloclips.com","nudeartstars.com","freeporndr.com","superdiosas.com","disco-girls.com","lewd-girls.com","mega-teen.com","heganporn.com","pornstarnirvna.com","llveleak.com","rude.com","anatarvasnavideos.com","tour.fuckmyindiangf.com","desindian.sextgem.com","iscindia.org","tubegogo.com","in.spankbang.com","yehfun.com","indiankahani.com","pornmdk.com","tubestack.com","desikahani.net","xesi.mobi","desitales.com","allindiansex.com","tubexclips.com","boyddl.com","comicmasala.com","slutload.com","befuck.com","porn20.org","allindiansexstories.com","cinedunia.com","bollywood-sex.net","funjadu.com","iloveindiansex.com","hyat.mobi","m.chudaimaza.com","adultphonechatlines.co.uk","fsiblog.com","fucking8.com","cloaktube.com","indianhotjokes.blogspot.in","wegret.com","indiansgoanal.org","desipapa.com","alizjokes.blogspot.in","jlobster.com","desikamasutra.com","myhotsite.net","hindi-sex.net","bullporn.com","oigh.info","jizzporntube.com","nonvegjokes.com","eeltube.com","haporntube.com","hindiold.com",)


}