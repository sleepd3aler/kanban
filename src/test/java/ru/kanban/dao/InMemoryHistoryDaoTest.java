package ru.kanban.dao;

class InMemoryHistoryDaoTest extends HistoryDaoTest {

    @Override
    HistoryDao createHistoryDao() {
        return new InMemoryHistoryDao();
    }

}