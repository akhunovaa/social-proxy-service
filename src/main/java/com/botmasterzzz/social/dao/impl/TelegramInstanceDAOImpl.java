package com.botmasterzzz.social.dao.impl;

import com.botmasterzzz.social.dao.TelegramInstanceDAO;
import com.botmasterzzz.social.entity.TelegramInstanceEntity;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TelegramInstanceDAOImpl implements TelegramInstanceDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public TelegramInstanceDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    public List<TelegramInstanceEntity> getFullTelegramInstanceList() {
        List<TelegramInstanceEntity> telegramInstanceEntity;
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(TelegramInstanceEntity.class);
        telegramInstanceEntity = criteria.list();
        session.close();
        return telegramInstanceEntity;
    }
}
