package com.project.springboot_thymeleaf.biz.roulette.mapper;

import com.project.springboot_thymeleaf.biz.roulette.dto.RouletteDrawHistoryDbDto;
import com.project.springboot_thymeleaf.biz.roulette.dto.RouletteEventDbDto;
import com.project.springboot_thymeleaf.biz.roulette.dto.RoulettePrizeDbDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RouletteMapper {

    RouletteEventDbDto selectCurrentEvent(@Param("todayYmd") String todayYmd);

    RouletteEventDbDto selectLatestEvent();

    List<RouletteEventDbDto> selectEventList();

    List<RoulettePrizeDbDto> selectPrizeListByEventId(@Param("eventId") Long eventId);

    String selectAcntNoByUsrIdAndEventId(@Param("usrId") String usrId, @Param("eventId") Long eventId);

    String selectAnyAcntNoByUsrId(@Param("usrId") String usrId);

    String selectTargetYn(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    int upsertDefaultTarget(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    String selectTargetCouponName(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    Integer selectRaffleTicketCount(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    int upsertDefaultRaffleBalance(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    int incrementRaffleTicketCount(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    int decrementRaffleTicketCount(@Param("eventId") Long eventId, @Param("acntNo") String acntNo);

    int insertDrawHistory(RouletteDrawHistoryDbDto historyDto);
}

