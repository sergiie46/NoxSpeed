# Play Store publishing checklist

- [ ] Create app `com.noxforgestudios.noxspeed` in Play Console.
- [ ] Create one-time product `premium_lifetime` and activate it for the intended countries/track.
- [ ] Create AdMob app + Banner + Interstitial + Rewarded units.
- [ ] Replace only the values in `monetization.properties`.
- [ ] Publish `LEGAL/privacy_es.html`, `privacy_en.html`, `terms_es.html`, `terms_en.html` on the NoxForge website.
- [ ] Put the final public URLs in `monetization.properties` and Play Console.
- [ ] Configure AdMob Privacy & messaging (EEA/UK/other required regions) so UMP has a message to load.
- [ ] Generate signed AAB with a private release keystore that is NOT committed into this project.
- [ ] Upload to Internal testing first and test Billing with licensed testers.
- [ ] Test consent forms with UMP debug/test configuration if needed.
- [ ] Confirm all production ad units replace test IDs before release.
- [ ] Complete Data Safety based on `data_safety_guide.md` and Google's latest SDK disclosure.
- [ ] Complete content rating and target audience questionnaires.
- [ ] Upload icon, feature graphic and phone screenshots from `GRAPHICS/`.
- [ ] Verify store listing in Spanish and English.
