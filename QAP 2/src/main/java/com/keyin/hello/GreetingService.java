package com.keyin.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GreetingService {
    private static final Logger logger = LoggerFactory.getLogger(GreetingService.class);

    @Autowired
    private GreetingRepository greetingRepository;

    @Autowired
    private LanguageRepository languageRepository;

    public Greeting getGreeting(long index) {
        Optional<Greeting> result = greetingRepository.findById(index);
        return result.orElse(null);
    }

    public Greeting createGreeting(Greeting newGreeting) {
        logger.debug("Creating new greeting: {}", newGreeting);

        if (newGreeting.getLanguages() == null) {
            Language english = languageRepository.findByName("English");

            if (english == null) {
                english = new Language();
                english.setName("English");
                languageRepository.save(english);
            }

            List<Language> languageList = new ArrayList<>();
            languageList.add(english);

            newGreeting.setLanguages(languageList);
        } else {
            for (Language language : newGreeting.getLanguages()) {
                Language langInDB = languageRepository.findByName(language.getName());
                if (langInDB == null) {
                    languageRepository.save(language);
                }
            }
        }

        return greetingRepository.save(newGreeting);
    }

    public List<Greeting> getAllGreetings() {
        return (List<Greeting>) greetingRepository.findAll();
    }

    public Greeting updateGreeting(Integer index, Greeting updatedGreeting) {
        logger.debug("Updating greeting at index {}: {}", index, updatedGreeting);

        Greeting greetingToUpdate = getGreeting(index);

        if (greetingToUpdate != null) {
            greetingToUpdate.setName(updatedGreeting.getName());
            greetingToUpdate.setGreeting(updatedGreeting.getGreeting());
            greetingToUpdate.setLanguages(updatedGreeting.getLanguages());
            return greetingRepository.save(greetingToUpdate);
        }
        return null;
    }

    public void deleteGreeting(long index) {
        logger.debug("Deleting greeting at index {}", index);

        Greeting greetingToDelete = getGreeting(index);
        if (greetingToDelete != null) {
            greetingRepository.delete(greetingToDelete);
        }
    }

    public List<Greeting> findGreetingsByNameAndGreeting(String name, String greetingName) {
        return greetingRepository.findByNameAndGreeting(name, greetingName);
    }

    public boolean addLanguageToGreeting(String language, Greeting greeting) {
        logger.debug("Attempting to add language '{}' to greeting '{}'", language, greeting);

        // Retrieve the existing greeting
        Greeting existingGreeting = getGreeting(greeting.getId());
        if (existingGreeting == null) {
            logger.warn("Greeting with ID {} not found", greeting.getId());
            return false;
        }

        logger.debug("Retrieved greeting: {}", existingGreeting);

        // Retrieve or create the language
        Language langInDB = languageRepository.findByName(language);
        if (langInDB == null) {
            langInDB = new Language();
            langInDB.setName(language);
            languageRepository.save(langInDB);
            logger.debug("Created new language: {}", langInDB);
        } else {
            logger.debug("Language already exists: {}", langInDB);
        }

        // Initialize languages list if null
        if (existingGreeting.getLanguages() == null) {
            existingGreeting.setLanguages(new ArrayList<>());
        }

        // Check if the language is already associated with the greeting
        boolean languageExists = existingGreeting.getLanguages().stream()
                .anyMatch(lang -> lang.getName().equals(language));

        if (languageExists) {
            logger.warn("Greeting already contains language '{}'", language);
            return false;
        }

        // Add the new language to the greeting and save it
        existingGreeting.getLanguages().add(langInDB);
        greetingRepository.save(existingGreeting);
        logger.info("Added language '{}' to greeting '{}'", language, existingGreeting);
        return true;
    }
}
