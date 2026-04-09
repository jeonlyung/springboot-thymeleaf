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

    String selectTargetYnByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);

    String selectTargetCouponNameByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);

    Integer selectRaffleTicketCountByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);

    int upsertDefaultRaffleBalanceByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);

    int incrementRaffleTicketCountByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);


    int decrementRaffleTicketCountByUsrId(@Param("eventId") Long eventId, @Param("usrId") String usrId);

    int insertDrawHistory(RouletteDrawHistoryDbDto historyDto);
}

